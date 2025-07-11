package com.example.fullstack.database.service.implementation;

import com.example.fullstack.config.OrderSequenceUtil;
import com.example.fullstack.database.dto.PaymentResponseDTO;
import com.example.fullstack.database.model.*;
import com.example.fullstack.database.repository.*;
import com.example.fullstack.database.service.OrdersService;
import com.example.fullstack.security.model.UserSecurity;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static com.example.fullstack.database.model.DiscountType.*;

@Service
public class OrdersServiceImpl implements OrdersService {

    private final CartRepository cartRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrdersRepository ordersRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final OrderSequenceUtil orderSequenceUtil;
    private final PaymentServiceImpl paymentServiceImpl;
    private final RestaurantInfoRepository restaurantInfoRepository;
    private final DiscountRepository discountRepository;
    private final DiscountUsageRepository discountUsageRepository;

    // ================== OPTIMIZED CONSTANTS ==================
    private static final BigDecimal MINIMUM_ORDER_AMOUNT = new BigDecimal("3500");
    private static final BigDecimal DELIVERY_FEE = new BigDecimal("400");
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final List<Status> EXCLUDED_ORDER_STATUSES = Arrays.asList(
            Status.PENDING, Status.CANCELLED, Status.DELIVERED, Status.COMPLETED, Status.FAILED
    );
    private static final List<Status> DELIVERED_COMPLETED_STATUSES = Arrays.asList(Status.DELIVERED, Status.COMPLETED);
    private static final List<Status> NON_PENDING_CANCELLED_STATUSES = Arrays.asList(Status.PENDING, Status.CANCELLED);

    // ================== CACHING FOR PERFORMANCE ==================
    private volatile boolean restaurantOpen = true;
    private long lastRestaurantCheck = 0;
    private static final long RESTAURANT_CHECK_INTERVAL = 60000; // 1 minute cache

    // Reusable StringBuilder for error messages
    private final ThreadLocal<StringBuilder> errorMessageBuilder = ThreadLocal.withInitial(() -> new StringBuilder(256));

    public OrdersServiceImpl(OrdersRepository ordersRepository, OrderItemRepository orderItemRepository,
                             CartRepository cartRepository, MenuItemRepository menuItemRepository,
                             UserRepository userRepository, AddressRepository addressRepository,
                             OrderSequenceUtil orderSequenceUtil, PaymentServiceImpl paymentServiceImpl,
                             RestaurantInfoRepository restaurantInfoRepository,
                             DiscountRepository discountRepository, DiscountUsageRepository discountUsageRepository) {
        this.ordersRepository = ordersRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.orderSequenceUtil = orderSequenceUtil;
        this.orderItemRepository = orderItemRepository;
        this.paymentServiceImpl = paymentServiceImpl;
        this.restaurantInfoRepository = restaurantInfoRepository;
        this.discountRepository = discountRepository;
        this.discountUsageRepository = discountUsageRepository;
    }

    // ================== BASIC CRUD OPERATIONS ==================

    @Override
    public void addOrder(Orders order) {
        if (order.getOrderSequence() == null) {
            order.setOrderSequence(orderSequenceUtil.getNextOrderSequence());
        }
        ordersRepository.save(order);
    }

    @Override
    public List<Orders> addAllOrders(List<Orders> orders) {
        if (orders.isEmpty()) {
            return orders;
        }

        // Pre-process all orders to avoid repeated sequence generation
        orders.forEach(order -> {
            if (order.getOrderSequence() == null) {
                order.setOrderSequence(orderSequenceUtil.getNextOrderSequence());
            }
        });
        return ordersRepository.saveAll(orders);
    }

    @Override
    public void updateOrder(Orders order) {
        ordersRepository.save(order);
    }

    @Override
    public Orders getOrderById(String orderId) {
        return ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    // ================== OPTIMIZED ORDER STATUS MANAGEMENT ==================

    @Override
    public void updateOrderStatus(String orderId, Status status) {
        // Early validation - cheapest checks first
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new RuntimeException("Order ID cannot be null or empty");
        }
        if (status == null) {
            throw new RuntimeException("Status cannot be null");
        }

        Orders order = getOrderById(orderId);
        validateOrderStatusUpdate(order, status);

        // Handle refund for cancelled orders
        if (status == Status.CANCELLED && order.getTransactionId() != null && order.getStatus() == Status.PLACED) {
            processRefund(order);
        }

        order.setStatus(status);
        ordersRepository.save(order);
    }

    private void validateOrderStatusUpdate(Orders order, Status newStatus) {
        Status currentStatus = order.getStatus();

        // Early exits for invalid state changes
        if (currentStatus == Status.CANCELLED) {
            throw new RuntimeException(buildErrorMessage("Cannot change status - order already cancelled: ", order.getOrderId()));
        }

        if (currentStatus == Status.DELIVERED) {
            throw new RuntimeException(buildErrorMessage("Order already delivered: ", order.getOrderId()));
        }

        if (currentStatus.equals(newStatus)) {
            throw new RuntimeException(buildErrorMessage("Cannot update order ", order.getOrderId(),
                    ": status is already ", newStatus.toString()));
        }
    }

    // ================== OPTIMIZED CHECKOUT PROCESS ==================

    @Override
    @Transactional
    public Orders processCheckoutWithDelivery() {
        return processCheckout(OrderType.DELIVERY);
    }

    @Override
    @Transactional
    public Orders processCheckoutWithPickup() {
        return processCheckout(OrderType.PICKUP);
    }

    private Orders processCheckout(OrderType orderType) {
        // Early validation - fail fast
        validateRestaurantOpen();

        User user = getCurrentAuthenticatedUser();
        Cart cart = getUserCart(user);

        // Validate cart before any expensive operations
        validateCartForCheckout(cart);

        Orders order = getOrCreatePendingOrder(user, orderType);
        setupOrderBasics(order, orderType);

//        if (orderType == OrderType.DELIVERY) {
//            setDeliveryAddress(order, user);
//        }

        // Optimized order item creation
        List<OrderItem> orderItems = createOrderItemsFromCartOptimized(cart, order);
        order.getOrderItems().clear();
        order.getOrderItems().addAll(orderItems);

        calculateAndSetOrderPricingOptimized(order, orderItems, orderType);

        return ordersRepository.save(order);
    }

    @Override
    @Transactional
    public Orders confirmCheckout(String orderId, PaymentMethod paymentMethod, OrderType orderType, String cardToken) {
        return confirmCheckoutInternal(orderId, paymentMethod, orderType, cardToken, null);
    }

    @Override
    @Transactional
    public Orders confirmCheckoutWithDiscount(String orderId, PaymentMethod paymentMethod, OrderType orderType,
                                              String cardToken, String discountCode) {
        return confirmCheckoutInternal(orderId, paymentMethod, orderType, cardToken, discountCode);
    }

    @Override
    @Transactional
    public Orders retryPayment(String orderId, String cardToken) {
        return confirmCheckout(orderId, PaymentMethod.CREDIT_CARD, null, cardToken);
    }

    private Orders confirmCheckoutInternal(String orderId, PaymentMethod paymentMethod, OrderType orderType,
                                           String cardToken, String discountCode) {
        // ================== OPTIMIZED EARLY VALIDATION ==================
        // 1. Validate inputs first (cheapest operations)
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new RuntimeException("Order ID cannot be null or empty");
        }

        if (paymentMethod == PaymentMethod.CREDIT_CARD && (cardToken == null || cardToken.trim().isEmpty())) {
            throw new RuntimeException("Card token is required for card payments");
        }

        if (discountCode != null && discountCode.trim().isEmpty()) {
            throw new RuntimeException("Discount code cannot be empty");
        }

        // 2. External state validation
        validateRestaurantOpen();
        User user = getCurrentAuthenticatedUser();
        Orders order = validateOrderForConfirmation(orderId, user);

        OrderType finalOrderType = orderType != null ? orderType : order.getOrderType();

        // 3. Validate discount BEFORE expensive operations
        Discount discount = null;
        BigDecimal discountAmount = ZERO;
        if (discountCode != null && !discountCode.trim().isEmpty()) {
            discount = validateAndApplyDiscount(discountCode, user, order);
            discountAmount = calculateDiscountAmount(discount, order.getOrderItems());
        }

        // 4. Setup order only after all validations pass
        setupOrderForConfirmation(order, finalOrderType, paymentMethod, user);

        if (discount != null) {
            order.setDiscount(discount);
            order.setDiscountAmount(discountAmount);
        }

        calculateFinalOrderPricingOptimized(order, finalOrderType, discountAmount);

        // Process payment if required
        if (paymentMethod == PaymentMethod.CREDIT_CARD) {
            processCardPayment(order, cardToken);
        }

        Orders confirmedOrder = ordersRepository.save(order);

        // Record discount usage and clear cart
        if (order.getDiscount() != null) {
            recordDiscountUsage(order.getDiscount(), user, confirmedOrder);
        }
        clearUserCart(user);

        return confirmedOrder;
    }

    // ================== ORDER RETRIEVAL METHODS ==================

    @Override
    public List<Orders> getAllOrders() {
        return ordersRepository.findByStatusNotInOrderByCreatedAtDesc(EXCLUDED_ORDER_STATUSES);
    }

    @Override
    public List<Orders> getOrdersByUser(String email) {
        // Early validation
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email cannot be null or empty");
        }

        validateUserAuthentication();
        List<Orders> orders = ordersRepository.findByUserEmailAndStatusNotOrderByCreatedAtDesc(email, Status.PENDING);

        if (orders.isEmpty()) {
            throw new RuntimeException("Customer has no orders");
        }
        return orders;
    }

    @Override
    public List<Orders> getAlLPlacedOrders() {
        return ordersRepository.findByStatusOrderByCreatedAtDesc(Status.PLACED);
    }

    @Override
    public List<Orders> getAllDeliveredOrders() {
        return ordersRepository.findByStatusOrderByCreatedAtDesc(Status.DELIVERED);
    }

    @Override
    public List<Orders> getAllCancelledOrders() {
        return ordersRepository.findByStatusOrderByCreatedAtDesc(Status.CANCELLED);
    }

    @Override
    public List<Orders> getAllDeliveredAndCompletedOrders() {
        return ordersRepository.findByStatusInOrderByCreatedAtDesc(DELIVERED_COMPLETED_STATUSES);
    }

    @Override
    @Transactional
    public void deletePendingOrders() {
        List<Orders> pendingOrders = ordersRepository.findByStatus(Status.PENDING);

        if (pendingOrders.isEmpty()) {
            throw new RuntimeException("No pending orders found");
        }

        // Batch delete order items first
        List<OrderItem> allOrderItems = new ArrayList<>();
        for (Orders order : pendingOrders) {
            allOrderItems.addAll(order.getOrderItems());
        }

        if (!allOrderItems.isEmpty()) {
            orderItemRepository.deleteAll(allOrderItems);
        }

        ordersRepository.deleteAll(pendingOrders);
    }

    // ================== OPTIMIZED HELPER METHODS ==================

    private User getCurrentAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserSecurity)) {
            throw new RuntimeException("User must be logged in");
        }

        UserSecurity userSecurity = (UserSecurity) principal;
        return userRepository.findByEmail(userSecurity.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void validateUserAuthentication() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserSecurity)) {
            throw new RuntimeException("User must be logged in");
        }
    }

    private Cart getUserCart(User user) {
        return cartRepository.findByUser(user).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No cart found for the user"));
    }

    private void validateCartForCheckout(Cart cart) {
        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("No items in the cart to checkout");
        }

        BigDecimal cartTotal = calculateCartItemsTotalOptimized(cart);
        if (cartTotal.compareTo(MINIMUM_ORDER_AMOUNT) < 0) {
            throw new RuntimeException(buildErrorMessage(
                    "Minimum order amount is ", MINIMUM_ORDER_AMOUNT.toString(),
                    " HUF. Please add more items to reach the minimum."
            ));
        }
    }

    // ================== OPTIMIZED CALCULATION METHODS ==================

    private BigDecimal calculateCartItemsTotalOptimized(Cart cart) {
        BigDecimal total = ZERO;

        // Single loop instead of multiple stream operations
        for (CartItem cartItem : cart.getCartItems()) {
            BigDecimal itemTotal = calculateCartItemTotalOptimized(cartItem);
            total = total.add(itemTotal);
        }

        return total;
    }

    private BigDecimal calculateCartItemTotalOptimized(CartItem cartItem) {
        BigDecimal basePrice = cartItem.getMenuItem().getPrice();
        BigDecimal extrasPrice = ZERO;

        if (cartItem.getExtras() != null && !cartItem.getExtras().isEmpty()) {
            for (Extra extra : cartItem.getExtras()) {
                extrasPrice = extrasPrice.add(extra.getPrice());
            }
        }

        return basePrice.add(extrasPrice).multiply(BigDecimal.valueOf(cartItem.getQuantity()));
    }

    private Orders getOrCreatePendingOrder(User user, OrderType orderType) {
        Optional<Orders> existingOrder = ordersRepository.findTopByUserAndStatusOrderByCreatedAtDesc(user, Status.PENDING);

        if (existingOrder.isPresent()) {
            Orders order = existingOrder.get();
            order.getOrderItems().clear();
            order.setOrderType(orderType);
            order.setUpdatedAt(LocalDateTime.now());
            return order;
        }

        return createNewOrder(user, orderType);
    }

    private Orders createNewOrder(User user, OrderType orderType) {
        LocalDateTime now = LocalDateTime.now();
        Orders order = new Orders();
        order.setUser(user);
        order.setStatus(Status.PENDING);
        order.setCreatedAt(now);
        order.setDate(LocalDate.now());
        order.setOrderType(orderType);
        order.setUpdatedAt(now);
        return order;
    }

    private void setupOrderBasics(Orders order, OrderType orderType) {
        order.setOrderType(orderType);
        order.setDeliveryFee(orderType == OrderType.DELIVERY ? DELIVERY_FEE : ZERO);
    }

    private void setDeliveryAddress(Orders order, User user) {
        Address selectedAddress = addressRepository.findByUserAndSelected(user, true);
        if (selectedAddress == null) {
            throw new RuntimeException("No selected address found for delivery");
        }
        order.setAddress(selectedAddress);
    }

    private List<OrderItem> createOrderItemsFromCartOptimized(Cart cart, Orders order) {
        List<CartItem> cartItems = cart.getCartItems();
        List<OrderItem> orderItems = new ArrayList<>(cartItems.size()); // Pre-size collection

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = createOrderItemFromCartItemOptimized(cartItem, order);
            orderItems.add(orderItem);
        }

        return orderItems;
    }

    private OrderItem createOrderItemFromCartItemOptimized(CartItem cartItem, Orders order) {
        OrderItem orderItem = new OrderItem();
        orderItem.setMenuItem(cartItem.getMenuItem());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setOrder(order);

        BigDecimal basePrice = cartItem.getMenuItem().getPrice();
        BigDecimal extrasPrice = ZERO;

        if (cartItem.getExtras() != null && !cartItem.getExtras().isEmpty()) {
            orderItem.setExtras(new ArrayList<>(cartItem.getExtras()));
            for (Extra extra : cartItem.getExtras()) {
                extrasPrice = extrasPrice.add(extra.getPrice());
            }
        }

        orderItem.setPricePerItem(basePrice.add(extrasPrice));
        return orderItem;
    }

    private void calculateAndSetOrderPricingOptimized(Orders order, List<OrderItem> orderItems, OrderType orderType) {

        BigDecimal itemsTotal = calculateOrderItemsTotalOptimized(orderItems);
        BigDecimal deliveryFee = orderType == OrderType.DELIVERY ? getTimeBasedDeliveryFee() : ZERO;
        BigDecimal serviceFee = order.getServiceFee() != null ? order.getServiceFee() : ZERO;
        BigDecimal bottleDepositFee = order.getTotalBottleDepositFee() != null ? order.getTotalBottleDepositFee() : ZERO;
        BigDecimal packagingFee = order.getPackageFee() != null ? order.getPackageFee() : ZERO;

        order.setTotalCartAmount(itemsTotal);
        order.setDeliveryFee(deliveryFee);
        order.setServiceFee(serviceFee);
        order.setPackageFee(packagingFee);
        order.setBottleDepositFee(bottleDepositFee);
        order.setTotalPrice(itemsTotal.add(deliveryFee).add(serviceFee).add(bottleDepositFee).add(packagingFee));
    }

    private BigDecimal getTimeBasedDeliveryFee() {
        LocalTime currentTime = LocalTime.now();
        int currentHour = currentTime.getHour();

        // Night rate: 12:00 AM (00:00) to 11:59 AM (11:59)
        // Day rate: 12:00 PM (12:00) to 11:59 PM (23:59)
        if (currentHour < 12) {
            return new BigDecimal(799); // Night rate (after midnight until noon)
        } else {
            return new BigDecimal(599); // Day rate (noon until midnight)
        }
    }

    private BigDecimal calculateOrderItemsTotalOptimized(List<OrderItem> orderItems) {
        BigDecimal total = ZERO;

        for (OrderItem item : orderItems) {
            BigDecimal itemTotal = item.getPricePerItem().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(itemTotal);
        }

        return total;
    }

    private Orders validateOrderForConfirmation(String orderId, User user) {
        Orders order = getOrderById(orderId);

        // Early validation checks
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: This order does not belong to the current user");
        }

        if (order.getStatus() == Status.PLACED) {
            throw new RuntimeException("This order has already been placed");
        }

        if (order.getStatus() != Status.PENDING) {
            throw new RuntimeException("Only pending orders can be confirmed");
        }

        return order;
    }

    private void setupOrderForConfirmation(Orders order, OrderType orderType, PaymentMethod paymentMethod, User user) {
        if (orderType == OrderType.DELIVERY) {
            setDeliveryAddress(order, user);
        } else {
            order.setAddress(null);
        }

        order.setPaymentMethod(paymentMethod);
        order.setOrderType(orderType);
        order.setOrderSequence(orderSequenceUtil.getNextOrderSequence());
        order.setStatus(paymentMethod == PaymentMethod.CASH ? Status.PLACED : Status.PENDING);
        order.setUpdatedAt(LocalDateTime.now());
    }

    private void calculateFinalOrderPricingOptimized(Orders order, OrderType orderType, BigDecimal discountAmount) {
        BigDecimal deliveryFee = orderType == OrderType.DELIVERY ? getTimeBasedDeliveryFee() : ZERO;
        BigDecimal serviceFee = order.getServiceFee() != null ? order.getServiceFee() : ZERO;
        BigDecimal bottleDepositFee = order.getTotalBottleDepositFee() != null ? order.getTotalBottleDepositFee() : ZERO;
        BigDecimal itemsTotal = calculateOrderItemsTotalOptimized(order.getOrderItems());
        BigDecimal packagingFee = order.getPackageFee() != null ? order.getPackageFee() : ZERO;

        order.setTotalCartAmount(itemsTotal);
        order.setDeliveryFee(deliveryFee);
        order.setServiceFee(serviceFee);
        order.setBottleDepositFee(bottleDepositFee);
        order.setPackageFee(packagingFee);

        // Single calculation chain
        BigDecimal totalPrice = itemsTotal
                .add(deliveryFee)
                .add(serviceFee)
                .add(bottleDepositFee)
                .add(packagingFee)
                .subtract(discountAmount);

        order.setTotalPrice(totalPrice.compareTo(ZERO) < 0 ? ZERO : totalPrice);
    }

    private void processCardPayment(Orders order, String cardToken) {
        // Refund existing transaction if present
        if (order.getTransactionId() != null) {
            processRefund(order);
        }

        PaymentResponseDTO paymentResponse = paymentServiceImpl.chargeCard(cardToken, order.getTotalPrice());
        if (!"COMPLETED".equals(paymentResponse.getStatus())) {
            throw new RuntimeException(buildErrorMessage("Payment failed: ", paymentResponse.getMessage()));
        }
        order.setTransactionId(paymentResponse.getTransactionId());
    }

    private void processRefund(Orders order) {
        PaymentResponseDTO refundResponse = paymentServiceImpl.refundPayment(
                order.getTransactionId(), order.getTotalPrice());
        if (!"COMPLETED".equals(refundResponse.getStatus())) {
            throw new RuntimeException(buildErrorMessage("Failed to refund payment: ", refundResponse.getMessage()));
        }
        order.setTransactionId(null);
    }

    private void clearUserCart(User user) {
        if (cartRepository.existsByUser(user)) {
            cartRepository.deleteByUser(user);
        }
    }

    // ================== OPTIMIZED RESTAURANT VALIDATION WITH CACHING ==================
    private void validateRestaurantOpen() {
        long now = System.currentTimeMillis();

        // Use cached value if within interval
        if (now - lastRestaurantCheck > RESTAURANT_CHECK_INTERVAL) {
            restaurantOpen = restaurantInfoRepository.findAll().stream()
                    .findFirst()
                    .map(RestaurantInfo::isOpen)
                    .orElse(false);
            lastRestaurantCheck = now;
        }

        if (!restaurantOpen) {
            throw new IllegalStateException("Sorry! The restaurant is currently closed and not accepting new orders.");
        }
    }

    // ================== OPTIMIZED DISCOUNT METHODS ==================

    private Discount validateAndApplyDiscount(String discountCode, User user, Orders order) {
        // Early validation - check code format first
        if (discountCode.length() > 50) { // Assuming reasonable code length limit
            throw new RuntimeException("Invalid discount code format");
        }

        Discount discount = discountRepository.findByDiscountCodeAndActiveTrue(discountCode)
                .orElseThrow(() -> new RuntimeException(buildErrorMessage("Invalid or inactive discount code: ", discountCode)));

        // Validate in order of likelihood to fail (most likely to least likely)
        validateDiscountTiming(discount);
        validateDiscountUsageLimit(discount);
        validateDiscountUserEligibility(discount, user);
        validateDiscountOrderType(discount, order);
        validateDiscountMinimumAmount(discount, order.getOrderItems());
        validateFirstOrderDiscount(discount, user);

        // Increment usage count
        discount.setCurrentUses(discount.getCurrentUses() + 1);
        discountRepository.save(discount);

        return discount;
    }

    private void validateDiscountTiming(Discount discount) {
        LocalDateTime now = LocalDateTime.now();

        if (discount.getValidFrom() != null && now.isBefore(discount.getValidFrom())) {
            throw new RuntimeException("Discount code is not yet valid");
        }

        if (discount.getValidUntil() != null && now.isAfter(discount.getValidUntil())) {
            throw new RuntimeException("Discount code has expired");
        }
    }

    private void validateDiscountUsageLimit(Discount discount) {
        if (discount.getMaxUses() != null && discount.getCurrentUses() >= discount.getMaxUses()) {
            throw new RuntimeException("Discount code has reached its usage limit");
        }
    }

    private void validateDiscountUserEligibility(Discount discount, User user) {
        if (discountUsageRepository.existsByDiscountAndUser(discount, user) ||
                discountUsageRepository.existsByDiscountAndUserPhone(discount, user.getPhone())) {
            throw new RuntimeException("This discount code has already been used by this user");
        }
    }

    private void validateDiscountOrderType(Discount discount, Orders order) {
        if (discount.getApplicableOrderType() != null &&
                discount.getApplicableOrderType() != order.getOrderType()) {
            throw new RuntimeException("Discount code is not applicable for this order type");
        }
    }

    private void validateDiscountMinimumAmount(Discount discount, List<OrderItem> orderItems) {
        BigDecimal cartItemsTotal = calculateOrderItemsTotalOptimized(orderItems);
        if (cartItemsTotal.compareTo(discount.getMinimumOrderAmount()) < 0) {
            throw new RuntimeException("Order amount does not meet the minimum requirement for this discount");
        }
    }

    private void validateFirstOrderDiscount(Discount discount, User user) {
        if (discount.getDiscountType() == FIRST_ORDER) {
            List<Orders> userOrders = ordersRepository.findByUserAndStatusNotIn(user, NON_PENDING_CANCELLED_STATUSES);
            if (!userOrders.isEmpty()) {
                throw new RuntimeException("This discount is only valid for first orders");
            }
        }
    }

    private BigDecimal calculateDiscountAmount(Discount discount, List<OrderItem> orderItems) {
        BigDecimal cartItemsTotal = calculateOrderItemsTotalOptimized(orderItems);
        BigDecimal discountAmount = ZERO;

        switch (discount.getDiscountType()) {
            case FIXED_AMOUNT:
                discountAmount = discount.getDiscountValue();
                break;
            case PERCENTAGE:
            case FIRST_ORDER:
            case SEASONAL:
                discountAmount = cartItemsTotal.multiply(
                        discount.getDiscountValue().divide(ONE_HUNDRED));
                break;
        }

        // Ensure discount doesn't exceed cart total
        return discountAmount.compareTo(cartItemsTotal) > 0 ? cartItemsTotal : discountAmount;
    }
    @Async
    protected void recordDiscountUsage(Discount discount, User user, Orders order) {
        DiscountUsage usage = new DiscountUsage();
        usage.setDiscount(discount);
        usage.setUser(user);
        usage.setUserPhone(user.getPhone());
        usage.setOrderId(order.getOrderId());
        usage.setUsedAt(LocalDateTime.now());
        discountUsageRepository.save(usage);
    }

    // ================== OPTIMIZED UTILITY METHODS ==================

    private String buildErrorMessage(String... parts) {
        StringBuilder sb = errorMessageBuilder.get();
        sb.setLength(0); // Clear previous content

        for (String part : parts) {
            sb.append(part);
        }

        return sb.toString();
    }

    // ================== DEPRECATED METHODS (kept for compatibility) ==================

    public User getCurrentUser() {
        return getCurrentAuthenticatedUser();
    }
}