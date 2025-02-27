package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.model.OrderItem;
import com.example.fullstack.database.model.Orders;
import com.example.fullstack.database.repository.OrderItemRepository;
import com.example.fullstack.database.repository.OrdersRepository;
import com.example.fullstack.database.service.OrderItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class OrderItemServiceImpl implements OrderItemService {

    OrderItemRepository orderItemRepository;
    OrdersRepository ordersRepository;
    public OrderItemServiceImpl(OrderItemRepository orderItemRepository, OrdersRepository ordersRepository) {
        this.orderItemRepository = orderItemRepository;
        this.ordersRepository = ordersRepository;
    }
    @Override
    public void createOrderItem(OrderItem orderItem) {
        orderItemRepository.save(orderItem);

    }

    @Override
    public void increaseItemQuantity(Long orderId, Long menuItemId, Integer quantity) {
        Orders order = ordersRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        OrderItem orderItem = order.getOrderItems().stream()
                .filter(item -> item.getMenuItem().getId().equals(String.valueOf(item.getId())))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Order item not found"));

        orderItem.setQuantity(orderItem.getQuantity() + quantity);
        orderItem.setTotalPrice(orderItem.getPricePerItem().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        ordersRepository.save(order);
        updateOrderTotalPrice(order);

    }
    @Override
    public void decreaseItemQuantity(Long orderId, Long menuItemId, Integer quantity) {
        Orders order = ordersRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        OrderItem orderItem = order.getOrderItems().stream()
                .filter(item -> item.getMenuItem().getId().equals(String.valueOf(item.getId())))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Order item not found"));

        if (orderItem.getQuantity() > quantity) {
            orderItem.setQuantity(orderItem.getQuantity() - quantity);
            orderItem.setTotalPrice(orderItem.getPricePerItem().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        } else {
            // for 0 quantity..
            order.getOrderItems().remove(orderItem);
            orderItemRepository.delete(orderItem);
        }
        orderItemRepository.save(orderItem);
        updateOrderTotalPrice(order);

    }

    @Override
    public void updateOrderTotalPrice(Orders order) {
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            BigDecimal totalPrice = order.getOrderItems().stream()
                    .map(OrderItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            order.setTotalPrice(totalPrice);
            ordersRepository.save(order);
        } else {
            order.setTotalPrice(BigDecimal.ZERO);
            ordersRepository.save(order);
        }
    }

}
