package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.model.*;
import com.example.fullstack.database.repository.*;
import com.example.fullstack.database.service.OrdersService;
import com.example.fullstack.security.model.UserSecurity;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrdersServiceImpl implements OrdersService {

    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;
     OrdersRepository ordersRepository;
     private final UserRepository userRepository;



    public OrdersServiceImpl(OrdersRepository ordersRepository, OrderItemRepository orderItemRepository
            , CartRepository cartRepository
            , MenuItemRepository menuItemRepository
            , UserRepository userRepository) {
        this.ordersRepository = ordersRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.menuItemRepository = menuItemRepository;
        this.userRepository = userRepository;
    }



    @Override
    public void addOrder(Orders order) {
        ordersRepository.save(order);
    }

    @Override
    public List<Orders> addAllOrders(List<Orders> orders) {
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
    public Orders processCheckout(HttpSession session, PaymentMethod paymentMethod, String address) {
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
        order.setPaymentMethod(paymentMethod);
        order.setAddress(address);
        order.setStatus(Status.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setTotalPrice(totalPrice);
        order.setDate(LocalDate.now());

        // Save the order to the database
        ordersRepository.save(order);

        // Create order items from cart items
        for (CartItem cartItem : cart.getCartItems()) {
            createOrderItemFromCart(cartItem, order);
        }

        // Clear the cart
        cartRepository.deleteByUser(user);

        return order;
    }
    private void createOrderItemFromCart(CartItem cartItem, Orders order) {
        OrderItem orderItem = new OrderItem();
        orderItem.setMenuItem(cartItem.getMenuItem());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setPricePerItem(cartItem.getMenuItem().getPrice());
        orderItem.setOrder(order);  // Associate with the order
        orderItemRepository.save(orderItem);
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


}
