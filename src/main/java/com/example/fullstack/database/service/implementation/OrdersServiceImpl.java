    package com.example.fullstack.database.service.implementation;

    import com.example.fullstack.config.OrderSequenceUtil;
    import com.example.fullstack.database.dto.PaymentResponseDTO;
    import com.example.fullstack.database.model.*;
    import com.example.fullstack.database.repository.*;
    import com.example.fullstack.database.service.OrdersService;
    import com.example.fullstack.security.model.UserSecurity;
    import jakarta.transaction.Transactional;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.stereotype.Service;

    import java.math.BigDecimal;
    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.util.*;

    @Service
    public class OrdersServiceImpl implements OrdersService {

        private final CartRepository cartRepository;
        private final OrderItemRepository orderItemRepository;
       private final  OrdersRepository ordersRepository;
         private final UserRepository userRepository;
         private final AddressRepository addressRepository;
         private final OrderSequenceUtil orderSequenceUtil;
         private final PaymentServiceImpl paymentServiceImpl ;



        public OrdersServiceImpl(OrdersRepository ordersRepository, OrderItemRepository orderItemRepository
                , CartRepository cartRepository
                , MenuItemRepository menuItemRepository
                , UserRepository userRepository
                , AddressRepository addressRepository
                , OrderSequenceUtil orderSequenceUtil,PaymentServiceImpl paymentServiceImpl) {
            this.ordersRepository = ordersRepository;
            this.cartRepository = cartRepository;
            this.userRepository = userRepository;
            this.addressRepository = addressRepository;
            this.orderSequenceUtil = orderSequenceUtil;
            this.orderItemRepository = orderItemRepository;
            this.paymentServiceImpl = paymentServiceImpl;
        }



        @Override
        public void addOrder(Orders order) {
            if (order.getOrderSequence() == null) {
                order.setOrderSequence(orderSequenceUtil.getNextOrderSequence());
            }
            ordersRepository.save(order);
        }

        @Override
        public List<Orders> addAllOrders(List<Orders> orders) {
            for (Orders order : orders) {
                if (order.getOrderSequence() == null) {
                    order.setOrderSequence(orderSequenceUtil.getNextOrderSequence());
                }
            }
            return ordersRepository.saveAll(orders);
        }

        @Override
        public void updateOrder(Orders order) {
            ordersRepository.save(order);
        }

        @Override
        public void updateOrderStatus(String orderId, Status status) {
            Orders order = ordersRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found" + orderId));

            if (status == Status.CANCELLED && order.getTransactionId() != null && order.getStatus() == Status.PLACED) {
                PaymentResponseDTO refundResponse = paymentServiceImpl.refundPayment(order.getTransactionId(), order.getTotalPrice());
                if (!"COMPLETED".equals(refundResponse.getStatus())) {
                    throw new RuntimeException("Failed to refund payment: " + refundResponse.getMessage());
                }
                order.setTransactionId(null); // Clear transactionId after refund
            }

            order.setStatus(status);
            ordersRepository.save(order);
        }


        //Checkout with cart
        @Override
        @Transactional
        public Orders processCheckout() {
            // Get user from JWT token
            UserSecurity userSecurity = (UserSecurity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = getUserFromUserSecurity(userSecurity);

            // Retrieve the user's cart
            Cart cart = cartRepository.findByUser(user).stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No cart found for the user"));

            if (cart.getCartItems().isEmpty()) {
                throw new RuntimeException("No items in the cart to checkout");
            }

            // Look for existing draft order
            Optional<Orders> existingOrder = ordersRepository.findTopByUserAndStatusOrderByCreatedAtDesc(user, Status.PENDING);
            Orders order;
            if (existingOrder.isPresent()) {
                order = existingOrder.get();
                order.getOrderItems().clear(); // Clear old items (thanks to orphanRemoval)
            } else {
                order = new Orders();
                order.setUser(user);
                order.setStatus(Status.PENDING);
                order.setCreatedAt(LocalDateTime.now());
                order.setDate(LocalDate.now());
                order.setDeliveryFee(BigDecimal.valueOf(400));
            }

            //Date and Time
            order.setUpdatedAt(LocalDateTime.now());

            // Reconstruct
            List<OrderItem> newOrderItems = new ArrayList<>();
            for (CartItem cartItem : cart.getCartItems()) {
                OrderItem orderItem = createOrderItemFromCart(cartItem, order);
                newOrderItems.add(orderItem);
            }

            //ADDS if more order items are added
            order.getOrderItems().addAll(newOrderItems);

            //Calculates Total Bottle Deposit fee 0 if no bottles or 50*no of bottles
            BigDecimal totalBottleDepositFee = order.getTotalBottleDepositFee();

            order.setTotalPrice(calculateTotalPrice(newOrderItems,order.getDeliveryFee(),order.getServiceFee(),totalBottleDepositFee)); // Recalculate total
    //      order.updateItemsFromCart(cart.getCartItems());
            return ordersRepository.save(order);
        }

        private OrderItem createOrderItemFromCart(CartItem cartItem, Orders order) {
            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(cartItem.getMenuItem());
            orderItem.setQuantity(cartItem.getQuantity());
            // Get the base price from menu item
            BigDecimal basePrice = cartItem.getMenuItem().getPrice();
            BigDecimal extrasPrice = BigDecimal.ZERO;

            // Calculate extras price if any
            if(cartItem.getExtras() != null && !cartItem.getExtras().isEmpty()) {
                orderItem.setExtras(new ArrayList<>(cartItem.getExtras()));
                for(Extra extra : cartItem.getExtras()) {
                    extrasPrice = extrasPrice.add(extra.getPrice());
                }
            }

            // Set the price per item (base + extras)
            BigDecimal totalPricePerItem = basePrice.add(extrasPrice);
            orderItem.setPricePerItem(totalPricePerItem);
            orderItem.setOrder(order);

            return orderItem;
        }

        @Override
        public List<Orders> getAllOrders() {
            List<Status> excludedStatuses = Arrays.asList(Status.PENDING, Status.CANCELLED);
            return ordersRepository.findByStatusNotInOrderByCreatedAtDesc(excludedStatuses);
        }

        @Override
        public List<Orders> getOrdersByUser(String email) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!(principal instanceof UserSecurity)) {
                throw new RuntimeException("User must be logged in to checkout");
            }

            // Convert UserSecurity to User
            UserSecurity userSecurity = (UserSecurity) principal;
            User user = getUserFromUserSecurity(userSecurity);
            List<Orders> order = ordersRepository.findByUserEmailAndStatusNotOrderByCreatedAtDesc(email, Status.PENDING);

            if(order.isEmpty()) {
                throw new RuntimeException("Customer Has no orders");
            }else {
                return order;
            }
        }

        @Override
        @Transactional
        public Orders retryPayment(String orderId, String cardToken) {
            return confirmCheckout(orderId, PaymentMethod.CREDIT_CARD, null, cardToken);
        }


        private BigDecimal calculateTotalPrice(List<OrderItem> orderItems,BigDecimal deliveryFee, BigDecimal serviceFee,BigDecimal totalBottleDepositFee) {
            //All the cart Items
            BigDecimal itemsTotal = orderItems.stream()
                    .map(item -> item.getPricePerItem().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            //Total Order Payment . Food+Delivery+service+bottle deposit
            return itemsTotal.add(deliveryFee)
                    .add(serviceFee)
                    .add(totalBottleDepositFee);
        }


        @Override
        @Transactional
        public Orders confirmCheckout(String orderId, PaymentMethod paymentMethod, OrderType orderType,String cardToken) {
            UserSecurity userSecurity = (UserSecurity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User currentUser = getUserFromUserSecurity(userSecurity);

            // Find the order first
            Orders pendingOrder = ordersRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            if (!pendingOrder.getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Unauthorized: This order does not belong to the current user");
            }

            if (pendingOrder.getStatus() == Status.PLACED) {
                throw new RuntimeException("This order has already been placed");
            }

            if (pendingOrder.getStatus() != Status.PENDING) {
                throw new RuntimeException("Only pending orders can be confirmed");
            }

            Address selectedAddress = addressRepository.findByUserAndSelected(currentUser, true);
            if (selectedAddress == null) {
                throw new RuntimeException("No selected address found for the current user");
            }

            // Set order details
            pendingOrder.setAddress(selectedAddress);
            pendingOrder.setPaymentMethod(paymentMethod);
            pendingOrder.setOrderType(orderType);
            pendingOrder.setOrderSequence(orderSequenceUtil.getNextOrderSequence());
            pendingOrder.setStatus(paymentMethod == PaymentMethod.CASH ? Status.PLACED : Status.PENDING);
            pendingOrder.setUpdatedAt(LocalDateTime.now());

            // Set fees based on order type
            BigDecimal deliveryFee = (orderType == OrderType.DELIVERY) ? BigDecimal.valueOf(400) : BigDecimal.ZERO;
            BigDecimal serviceFee = pendingOrder.getServiceFee(); // Keep existing service fee
            BigDecimal bottleDepositFee = pendingOrder.getTotalBottleDepositFee();

            // Set individual fees
            pendingOrder.setDeliveryFee(deliveryFee);
            pendingOrder.setServiceFee(serviceFee);
            pendingOrder.setBottleDepositFee(bottleDepositFee);

            // Calculate cart amount (items only, without fees)
            BigDecimal cartAmount = pendingOrder.getOrderItems().stream()
                    .map(item -> item.getPricePerItem().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            pendingOrder.setTotalCartAmount(cartAmount);

            // Calculate total price using the new method
            BigDecimal totalPrice = calculateTotalPriceBasedOnOrderType(
                    pendingOrder.getOrderItems(),
                    orderType,
                    deliveryFee,
                    serviceFee,
                    bottleDepositFee
            );
            pendingOrder.setTotalPrice(totalPrice);
            // Handle payment processing
            if (paymentMethod == PaymentMethod.CREDIT_CARD) {
                if (cardToken == null || cardToken.trim().isEmpty()) {
                    throw new RuntimeException("Card token is required for card payments");
                }

                // Refund previous successful payment if retrying
                if (pendingOrder.getTransactionId() != null) {
                    PaymentResponseDTO refundResponse = paymentServiceImpl.refundPayment(pendingOrder.getTransactionId(), totalPrice);
                    if (!"COMPLETED".equals(refundResponse.getStatus())) {
                        throw new RuntimeException("Failed to refund previous payment: " + refundResponse.getMessage());
                    }
                    pendingOrder.setTransactionId(null);
                }

                PaymentResponseDTO paymentResponse = paymentServiceImpl.chargeCard(cardToken, totalPrice);
                if (!"COMPLETED".equals(paymentResponse.getStatus())) {
                    throw new RuntimeException("Payment failed: " + paymentResponse.getMessage());
                }
                pendingOrder.setTransactionId(paymentResponse.getTransactionId()); // Store Revolut orderId
            }


            Orders confirmedOrder = ordersRepository.save(pendingOrder);

            // Clear the cart only after confirmation
            if(cartRepository.existsByUser(currentUser)) {
                cartRepository.deleteByUser(currentUser);
            }

            return confirmedOrder;
        }


        @Override
        public void deletePendingOrders() {
            List<Orders> pendingOrders  = ordersRepository.findByStatus(Status.PENDING);

            if(pendingOrders.isEmpty()) {
                throw new RuntimeException("No pending orders found");
            }

            for(Orders order : pendingOrders) {
                for(OrderItem orderItem : order.getOrderItems()) {
                    orderItemRepository.delete(orderItem);
                }
            }
            ordersRepository.deleteAll(pendingOrders);

        }

        private User getUserFromUserSecurity(UserSecurity userSecurity) {
            // Assuming UserSecurity has a method to get the user ID or username
            return userRepository.findByEmail(userSecurity.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Alternative if using username instead of ID
            // return userRepository.findByUsername(userSecurity.getUsername())
            //        .orElseThrow(() -> new RuntimeException("User not found"));
        }

        public User getCurrentUser() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                return (User) authentication.getPrincipal(); // Assuming UserDetails is being used
            }
            throw new RuntimeException("No authenticated user found");
        }

        @Override
        public Orders getOrderById(String orderId) {
            return ordersRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found" + orderId));
        }



//        public void updateItemsFromCart(List<CartItem> cartItems) {
//            //        this.orderItems.clear();     // remove old items and trigger orphanRemoval
//            //
//            //        for (CartItem cartItem : cartItems) {
//            //            OrderItem orderItem = new OrderItem();
//            //            orderItem.setOrder(this);
//            //            orderItem.setQuantity(cartItem.getQuantity());
//            //            orderItem.setPricePerItem(cartItem.getMenuItem().getPrice());
//            //            orderItem.setMenuItem(cartItem.getMenuItem());
//            //            orderItem.setExtras(new ArrayList<>(cartItem.getExtras())); // copy extras if needed
//            //            this.orderItems.add(orderItem);
//            //        }
//            //
//            //        // Optionally set total price here or let service do it
//            //        this.totalPrice = this.getTotalPrice();
//            //    }


        private BigDecimal calculateTotalPriceBasedOnOrderType(List<OrderItem> orderItems,OrderType orderType,BigDecimal deliveryFee, BigDecimal serviceFee,BigDecimal totalBottleDepositFee) {
            BigDecimal cartAmount = orderItems.stream()
                    .map(item -> item.getPricePerItem().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalPrice = cartAmount;

            switch(orderType){
                case DELIVERY:
                    totalPrice = cartAmount.add(deliveryFee != null ? deliveryFee : BigDecimal.ZERO);
                case PICKUP:
                case DINE_IN:
                    break;
                    default:
                    throw new IllegalArgumentException("Unsupported order type: " + orderType);
            }

            totalPrice = totalPrice.add(serviceFee != null ? serviceFee : BigDecimal.ZERO);

            totalPrice = totalPrice.add(totalBottleDepositFee != null ? totalBottleDepositFee : BigDecimal.ZERO);

            return totalPrice;
        }

    }
