package com.example.fullstack.database.service;

import com.example.fullstack.database.model.OrderItem;
import com.example.fullstack.database.model.Orders;

public interface OrderItemService {
    void createOrderItem(OrderItem orderItem);
    void increaseItemQuantity(Long orderId, Long menuItemId,Integer quantity);
    void decreaseItemQuantity(Long orderId, Long menuItemId,Integer quantity);
    void updateOrderTotalPrice(Orders order);
}
