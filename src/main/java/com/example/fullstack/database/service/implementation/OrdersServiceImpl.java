package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.model.*;
import com.example.fullstack.database.repository.OrderItemRepository;
import com.example.fullstack.database.repository.OrdersRepository;
import com.example.fullstack.database.service.OrdersService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrdersServiceImpl implements OrdersService {

    private final OrderItemRepository orderItemRepository;
    OrdersRepository ordersRepository;



    public OrdersServiceImpl(OrdersRepository ordersRepository, OrderItemRepository orderItemRepository) {
        this.ordersRepository = ordersRepository;
        this.orderItemRepository = orderItemRepository;
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
    public Orders processCheckout(List<OrderItem> orderItems, PaymentMethod paymentMethod,String address) {
        User user = getCurrentUser();
        Orders order = new Orders();
        order.setUser(user);
        order.setPaymentMethod(paymentMethod);
        order.setAddress(address);
        order.setStatus(Status.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());


        BigDecimal totalPrice = calculateTotalPrice(orderItems);
        order.setTotalPrice(totalPrice);
        Orders readyOrder = ordersRepository.save(order);

        for(OrderItem orderItem : orderItems) {
            orderItem.setOrder(readyOrder);
            orderItemRepository.save(orderItem);
        }


        return readyOrder;
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
