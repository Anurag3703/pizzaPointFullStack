package com.example.fullstack.database.service.implementation;

import com.example.fullstack.config.OrderSequenceUtil;
import com.example.fullstack.database.model.*;
import com.example.fullstack.database.repository.*;
import com.example.fullstack.database.service.OrdersService;
import com.example.fullstack.security.model.UserSecurity;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.aspectj.weaver.ast.Or;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrdersServiceImpl implements OrdersService {

    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;
     OrdersRepository ordersRepository;
     private final UserRepository userRepository;
     private final AddressRepository addressRepository;
     private OrderSequenceUtil orderSequenceUtil;



    public OrdersServiceImpl(OrdersRepository ordersRepository, OrderItemRepository orderItemRepository
            , CartRepository cartRepository
            , MenuItemRepository menuItemRepository
            , UserRepository userRepository
            , AddressRepository addressRepository
            , OrderSequenceUtil orderSequenceUtil) {
        this.ordersRepository = ordersRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.menuItemRepository = menuItemRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.orderSequenceUtil = orderSequenceUtil;

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

        order.setStatus(status);
        ordersRepository.save(order);
    }




    @Override
    @Transactional
    public Orders processCheckout(HttpSession session) {
        // Retrieve the current user
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserSecurity)) {
            throw new RuntimeException("User must be logged in to checkout");
        }

        // Convert UserSecurity to User
        UserSecurity userSecurity = (UserSecurity) principal;
        User user = getUserFromUserSecurity(userSecurity);

        // Retrieve the user's cart
        Cart cart = cartRepository.findByUser(user).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No cart found for the user"));

        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("No items in the cart to checkout");
        }

        // Calculate the total price of the cart
        BigDecimal totalPrice = cart.getCartItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create a new order
        Orders order = new Orders();
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setTotalPrice(totalPrice);
        order.setDate(LocalDate.now());

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = createOrderItemFromCart(cartItem, order, session); // Pass the session
            orderItems.add(orderItem);
        }
        order.setOrderItems(orderItems);
        session.setAttribute("pendingOrder", order);

        // Clear the cart
        //cartRepository.deleteByUser(user);

        return order;
    }

    @Override
    public List<Orders> getAllOrders() {
//        List<Orders> orders = ordersRepository.findAll();
//        Map<Status, Integer> statusPriority = new HashMap<>();
//
//        statusPriority.put(Status.PREPARING, 1);
//        statusPriority.put(Status.READY_FOR_PICKUP, 2);
//        statusPriority.put(Status.PLACED, 3);
//        statusPriority.put(Status.PENDING, 4);
//        statusPriority.put(Status.OUT_FOR_DELIVERY, 5);
//        statusPriority.put(Status.FAILED, 6);
//        statusPriority.put(Status.CANCELLED, 7);
//        statusPriority.put(Status.COMPLETED, 8);
//        statusPriority.put(Status.DELIVERED, 9);
//
//        // Sort orders based on status priority
//        orders.sort(Comparator.comparing(order ->
//                statusPriority.getOrDefault(order.getStatus(), Integer.MAX_VALUE)));
        return ordersRepository.findAll();
    }

    @Override
    public List<Orders> getOrdersByUser(Long userId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserSecurity)) {
            throw new RuntimeException("User must be logged in to checkout");
        }

        // Convert UserSecurity to User
        UserSecurity userSecurity = (UserSecurity) principal;
        User user = getUserFromUserSecurity(userSecurity);

        try {
             List<Orders> order = ordersRepository.findByUserId(userId);
             if(order.isEmpty()) {
                 throw new RuntimeException("Customer Has no orders");
             }else {
                 return order;
             }
        }catch (RuntimeException e) {
            throw e;
        }
    }

    private OrderItem createOrderItemFromCart(CartItem cartItem, Orders order, HttpSession session) {
        OrderItem orderItem = new OrderItem();
        orderItem.setMenuItem(cartItem.getMenuItem());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setPricePerItem(cartItem.getTotalPrice());
        orderItem.setOrder(order);  // Associate with the order
        if(cartItem.getExtras() != null && !cartItem.getExtras().isEmpty()) {
            orderItem.setExtras(new ArrayList<>(cartItem.getExtras()));
        }
        List<OrderItem> sessionOrderItems = (List<OrderItem>) session.getAttribute("orderItems");
        if(sessionOrderItems == null) {
            sessionOrderItems = new ArrayList<>();
        }

        sessionOrderItems.add(orderItem);
        session.setAttribute("orderItems", sessionOrderItems);
        return orderItem;
    }

    @Override
    public Orders getOrderById(String orderId) {
        return ordersRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found" + orderId));
    }



    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return (User) authentication.getPrincipal(); // Assuming UserDetails is being used
        }
        throw new RuntimeException("No authenticated user found");
    }

    private BigDecimal calculateTotalPrice(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(item -> item.getPricePerItem().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private User getUserFromUserSecurity(UserSecurity userSecurity) {
        // Assuming UserSecurity has a method to get the user ID or username
        return userRepository.findByEmail(userSecurity.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Alternative if using username instead of ID
        // return userRepository.findByUsername(userSecurity.getUsername())
        //        .orElseThrow(() -> new RuntimeException("User not found"));
    }


    @Override
    @Transactional
    public Orders confirmCheckout(PaymentMethod paymentMethod,  OrderType orderType, HttpSession session) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserSecurity)) {
            throw new RuntimeException("User must be logged in to confirm checkout");
        }

        // Convert UserSecurity to User
        UserSecurity userSecurity = (UserSecurity) principal;
        User currentUser = getUserFromUserSecurity(userSecurity);

        Orders order = (Orders) session.getAttribute("pendingOrder");
        if(order == null) {
            throw new RuntimeException("No pending order found in your session");
        }

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized: This order does not belong to the current user");
        }

        List<OrderItem> orderItems = (List<OrderItem>) session.getAttribute("orderItems");
        if(orderItems == null || orderItems.isEmpty()) {
            throw new RuntimeException("No order items found in your session");
        }

        Address selectedAddress = addressRepository.findByUserAndSelected(currentUser, true);
        if (selectedAddress == null) {
            throw new RuntimeException("No selected address found for the current user");
        }

        order.setAddress(selectedAddress);

        if (paymentMethod == null) {
            throw new RuntimeException("Payment method is required");
        }


        if (orderType == null) {
            throw new RuntimeException("Order type is required");
        }

        order.setOrderSequence(orderSequenceUtil.getNextOrderSequence());

        order.setPaymentMethod(paymentMethod);
        order.setOrderType(orderType);

        if (paymentMethod == PaymentMethod.CASH) {
            order.setStatus(Status.PLACED);
        } else {
            order.setStatus(Status.PENDING);
        }
        ordersRepository.save(order);

        for(OrderItem orderItem : orderItems) {
            orderItem.setOrder(order);
            orderItemRepository.save(orderItem);
        }

        session.removeAttribute("orderItems");
        session.removeAttribute("orderItems");

        cartRepository.deleteByUser(currentUser);

        return order;


    }

}
