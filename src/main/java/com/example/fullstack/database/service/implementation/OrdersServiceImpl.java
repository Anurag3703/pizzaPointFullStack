package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.model.*;
import com.example.fullstack.database.repository.CartRepository;
import com.example.fullstack.database.repository.MenuItemRepository;
import com.example.fullstack.database.repository.OrderItemRepository;
import com.example.fullstack.database.repository.OrdersRepository;
import com.example.fullstack.database.service.OrdersService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrdersServiceImpl implements OrdersService {

    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;
     OrdersRepository ordersRepository;



    public OrdersServiceImpl(OrdersRepository ordersRepository, OrderItemRepository orderItemRepository, CartRepository cartRepository, MenuItemRepository menuItemRepository) {
        this.ordersRepository = ordersRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.menuItemRepository = menuItemRepository;
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
    public void updateOrderStatus(Long orderId, Status status) {
        Orders order = ordersRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found" + orderId));

        order.setStatus(status);
        ordersRepository.save(order);
    }

    @Override
    public Orders processCheckout(PaymentMethod paymentMethod, String address) {
        User user = getCurrentUser();

        if (user == null) {
            throw new RuntimeException("No user is logged in");
        }

        // Retrieve the cart items for the current user from the database
        List<Cart> cartItems = cartRepository.findByUser(user);

        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("No items in the cart to checkout");
        }

        // Calculate the total price of the cart
        BigDecimal totalPrice = cartItems.stream()
                .map(Cart::getTotalPrice)
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

        // Save the order to the database
        ordersRepository.save(order);

        // Create order items from the cart and associate them with the order
        for (Cart cartItem : cartItems) {
            createOrderItemFromCart(cartItem, order);
        }

        // Clear the cart items for the user after successful checkout
        cartRepository.deleteByUser(user);

        return order;
    }

    private void createOrderItemFromCart(Cart cartItem, Orders order) {
        OrderItem orderItem = new OrderItem();
        orderItem.setMenuItem(cartItem.getMenuItem());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setPricePerItem(cartItem.getPricePerItem());
        orderItem.setOrder(order);  // Associate with the order
        orderItemRepository.save(orderItem);
    }

    @Override
    public Orders getOrderById(Long orderId) {
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


}
