package com.example.fullstack.database.service.implementation;

import com.example.fullstack.config.OrderSequenceUtil;
import com.example.fullstack.database.dto.PaymentResponseDTO;
import com.example.fullstack.database.model.*;
import com.example.fullstack.database.repository.*;
import com.example.fullstack.database.service.OrdersService;
import com.example.fullstack.security.model.UserSecurity;
import jakarta.transaction.Transactional;
import org.hibernate.query.Order;
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
    private final OrdersRepository ordersRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final OrderSequenceUtil orderSequenceUtil;
    private final PaymentServiceImpl paymentServiceImpl;
    private final CustomMealRepository customMealRepository;

    public OrdersServiceImpl(OrdersRepository ordersRepository, OrderItemRepository orderItemRepository,
                             CartRepository cartRepository, MenuItemRepository menuItemRepository,
                             UserRepository userRepository, AddressRepository addressRepository,
                             OrderSequenceUtil orderSequenceUtil, PaymentServiceImpl paymentServiceImpl, CustomMealRepository customMealRepository) {
        this.ordersRepository = ordersRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.orderSequenceUtil = orderSequenceUtil;
        this.orderItemRepository = orderItemRepository;
        this.paymentServiceImpl = paymentServiceImpl;
        this.customMealRepository = customMealRepository;
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
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (status == Status.CANCELLED && order.getTransactionId() != null && order.getStatus() == Status.PLACED) {
            PaymentResponseDTO refundResponse = paymentServiceImpl.refundPayment(order.getTransactionId(), order.getTotalPrice());
            if (!"COMPLETED".equals(refundResponse.getStatus())) {
                throw new RuntimeException("Failed to refund payment: " + refundResponse.getMessage());
            }
            order.setTransactionId(null);
        }

        order.setStatus(status);
        ordersRepository.save(order);
    }

    @Override
    @Transactional
    public Orders processCheckoutWithDelivery() {
        UserSecurity userSecurity = (UserSecurity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = getUserFromUserSecurity(userSecurity);
        Orders order = initializeOrder(user, OrderType.DELIVERY);

        Address selectedAddress = addressRepository.findByUserAndSelected(user, true);
        if (selectedAddress == null) {
            throw new RuntimeException("No selected address found for delivery");
        }
        order.setAddress(selectedAddress);

        Cart cart = cartRepository.findByUser(user).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No cart found for the user"));
        List<OrderItem> newOrderItems = createOrderItemsFromCart(cart, order);
        order.getOrderItems().addAll(newOrderItems);

        BigDecimal totalBottleDepositFee = order.getTotalBottleDepositFee();
        order.setTotalPrice(calculateTotalPrice(newOrderItems, order.getDeliveryFee(), order.getServiceFee(), totalBottleDepositFee));

        return ordersRepository.save(order);
    }

    @Override
    @Transactional
    public Orders processCheckoutWithPickup() {
        UserSecurity userSecurity = (UserSecurity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = getUserFromUserSecurity(userSecurity);
        Orders order = initializeOrder(user, OrderType.PICKUP);

         //Clear address for pickup orders
        order.setAddress(null);

        Cart cart = cartRepository.findByUser(user).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No cart found for the user"));
        List<OrderItem> newOrderItems = createOrderItemsFromCart(cart, order);
        order.getOrderItems().addAll(newOrderItems);

        BigDecimal totalBottleDepositFee = order.getTotalBottleDepositFee();
        order.setTotalPrice(calculateTotalPrice(newOrderItems, BigDecimal.ZERO, order.getServiceFee(), totalBottleDepositFee));

        return ordersRepository.save(order);
    }

    private Orders initializeOrder(User user, OrderType orderType) {
        Cart cart = cartRepository.findByUser(user).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No cart found for the user"));

        if (cart.getCartItems().isEmpty() && cart.getCustomMeals().isEmpty()) {
            throw new RuntimeException("No items or meals in the cart to checkout");
        }

        Optional<Orders> existingOrder = ordersRepository.findTopByUserAndStatusOrderByCreatedAtDesc(user, Status.PENDING);
        Orders order;
        if (existingOrder.isPresent()) {
            order = existingOrder.get();
            order.getOrderItems().clear();
            // ✅ FIX: Update the order type and delivery fee for existing orders
            order.setOrderType(orderType);
            order.setDeliveryFee(orderType == OrderType.PICKUP ? BigDecimal.ZERO : BigDecimal.valueOf(400));
        } else {
            order = new Orders();
            order.setUser(user);
            order.setStatus(Status.PENDING);
            order.setCreatedAt(LocalDateTime.now());
            order.setDate(LocalDate.now());
            order.setOrderType(orderType);
            order.setDeliveryFee(orderType == OrderType.PICKUP ? BigDecimal.ZERO : BigDecimal.valueOf(400));
        }

        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }

    private List<OrderItem> createOrderItemsFromCart(Cart cart, Orders order) {
        List<OrderItem> newOrderItems = new ArrayList<>();
        if (cart.getCartItems() != null) {
            for (CartItem cartItem : cart.getCartItems()) {
                if (cartItem.getMenuItem() != null) {
                    OrderItem orderItem = createOrderItemFromCart(cartItem, order);
                    newOrderItems.add(orderItem);
                }
            }
        }
        if (cart.getCustomMeals() != null) {
            for (CustomMeal customMeal : cart.getCustomMeals()) {
                if (customMeal != null && customMeal.getTemplate() != null) {
                    OrderItem orderItem = createOrderItemFromCustomMeal(customMeal, order);
                    newOrderItems.add(orderItem);
                }
            }
        }
        return newOrderItems;
    }

    private OrderItem createOrderItemFromCustomMeal(CustomMeal customMeal, Orders order) {
        if (customMeal == null || customMeal.getTemplate() == null) {
            throw new IllegalArgumentException("CustomMeal or its template cannot be null");
        }
        OrderItem orderItem = new OrderItem();
        orderItem.setCustomMeal(customMeal);
        orderItem.setQuantity(1L);
        orderItem.setPricePerItem(customMeal.getTotalPrice());
        orderItem.setOrder(order);
        customMeal.setCart(null);
        customMealRepository.save(customMeal);
        return orderItem;

    }

    private OrderItem createOrderItemFromCart(CartItem cartItem, Orders order) {
        OrderItem orderItem = new OrderItem();
        orderItem.setMenuItem(cartItem.getMenuItem());
        orderItem.setQuantity(cartItem.getQuantity());
        BigDecimal basePrice = cartItem.getMenuItem().getPrice();
        BigDecimal extrasPrice = BigDecimal.ZERO;

        if (cartItem.getExtras() != null && !cartItem.getExtras().isEmpty()) {
            orderItem.setExtras(new ArrayList<>(cartItem.getExtras()));
            for (Extra extra : cartItem.getExtras()) {
                extrasPrice = extrasPrice.add(extra.getPrice());
            }
        }

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
            throw new RuntimeException("User must be logged in to view order history");
        }

        UserSecurity userSecurity = (UserSecurity) principal;
        User user = getUserFromUserSecurity(userSecurity);

        // Verify the email matches the authenticated user
        if (!user.getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized access to order history");
        }

        List<Orders> orders = ordersRepository.findByUserEmailAndStatusNotOrderByCreatedAtDesc(email, Status.PENDING);

        // Don't throw exception for empty orders - return empty list instead
        return orders; // This will be an empty list if no orders found
    }

    @Override
    @Transactional
    public Orders retryPayment(String orderId, String cardToken) {
        return confirmCheckout(orderId, PaymentMethod.CREDIT_CARD, null, cardToken);
    }

    private BigDecimal calculateTotalPrice(List<OrderItem> orderItems, BigDecimal deliveryFee, BigDecimal serviceFee, BigDecimal totalBottleDepositFee) {
        BigDecimal itemsTotal = orderItems.stream()
                .map(item -> item.getPricePerItem().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return itemsTotal
                .add(deliveryFee != null ? deliveryFee : BigDecimal.ZERO)
                .add(serviceFee != null ? serviceFee : BigDecimal.ZERO)
                .add(totalBottleDepositFee != null ? totalBottleDepositFee : BigDecimal.ZERO);
    }

    @Override
    @Transactional
    public Orders confirmCheckout(String orderId, PaymentMethod paymentMethod, OrderType orderType, String cardToken) {
        UserSecurity userSecurity = (UserSecurity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = getUserFromUserSecurity(userSecurity);

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

        if (orderType == OrderType.DELIVERY) {
            Address selectedAddress = addressRepository.findByUserAndSelected(currentUser, true);
            if (selectedAddress == null) {
                throw new RuntimeException("No selected address found for delivery");
            }
            pendingOrder.setAddress(selectedAddress);
        }

        pendingOrder.setPaymentMethod(paymentMethod);
        pendingOrder.setOrderType(orderType);
        pendingOrder.setOrderSequence(orderSequenceUtil.getNextOrderSequence());
        pendingOrder.setStatus(paymentMethod == PaymentMethod.CASH ? Status.PLACED : Status.PENDING);
        pendingOrder.setUpdatedAt(LocalDateTime.now());

        BigDecimal deliveryFee = (orderType == OrderType.DELIVERY) ? BigDecimal.valueOf(400) : BigDecimal.ZERO;
        BigDecimal serviceFee = pendingOrder.getServiceFee();
        BigDecimal bottleDepositFee = pendingOrder.getTotalBottleDepositFee();

        pendingOrder.setDeliveryFee(deliveryFee);
        pendingOrder.setServiceFee(serviceFee);
        pendingOrder.setBottleDepositFee(bottleDepositFee);

        BigDecimal cartAmount = pendingOrder.getOrderItems().stream()
                .map(item -> item.getPricePerItem().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        pendingOrder.setTotalCartAmount(cartAmount);

        BigDecimal totalPrice = calculateTotalPriceBasedOnOrderType(
                pendingOrder.getOrderItems(),
                orderType,
                deliveryFee,
                serviceFee,
                bottleDepositFee
        );
        pendingOrder.setTotalPrice(totalPrice);

        if (paymentMethod == PaymentMethod.CREDIT_CARD) {
            if (cardToken == null || cardToken.trim().isEmpty()) {
                throw new RuntimeException("Card token is required for card payments");
            }

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
            pendingOrder.setTransactionId(paymentResponse.getTransactionId());
        }

        Orders confirmedOrder = ordersRepository.save(pendingOrder);

        if (cartRepository.existsByUser(currentUser)) {
            cartRepository.deleteByUser(currentUser);
        }

        return confirmedOrder;
    }

    @Override
    public void deletePendingOrders() {
        List<Orders> pendingOrders = ordersRepository.findByStatus(Status.PENDING);

        if (pendingOrders.isEmpty()) {
            throw new RuntimeException("No pending orders found");
        }

        for (Orders order : pendingOrders) {
            for (OrderItem orderItem : order.getOrderItems()) {
                orderItemRepository.delete(orderItem);
            }
        }
        ordersRepository.deleteAll(pendingOrders);
    }

    private User getUserFromUserSecurity(UserSecurity userSecurity) {
        return userRepository.findByEmail(userSecurity.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        throw new RuntimeException("No authenticated user found");
    }

    @Override
    public Orders getOrderById(String orderId) {
        return ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    private BigDecimal calculateTotalPriceBasedOnOrderType(List<OrderItem> orderItems, OrderType orderType,
                                                           BigDecimal deliveryFee, BigDecimal serviceFee,
                                                           BigDecimal totalBottleDepositFee) {
        BigDecimal cartAmount = orderItems.stream()
                .map(item -> item.getPricePerItem().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPrice = cartAmount;

        switch (orderType) {
            case DELIVERY:
                totalPrice = cartAmount.add(deliveryFee != null ? deliveryFee : BigDecimal.ZERO);
                break;
            case PICKUP:
            case DINE_IN:
                break;
            default:
                throw new IllegalArgumentException("Unsupported order type: " + orderType);
        }

        totalPrice = totalPrice.add(serviceFee != null ? serviceFee : BigDecimal.ZERO)
                .add(totalBottleDepositFee != null ? totalBottleDepositFee : BigDecimal.ZERO);

        return totalPrice;
    }
}