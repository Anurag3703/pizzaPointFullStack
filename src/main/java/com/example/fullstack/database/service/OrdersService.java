package com.example.fullstack.database.service;

import com.example.fullstack.database.model.OrderItem;
import com.example.fullstack.database.model.Orders;
import com.example.fullstack.database.model.Status;
import org.aspectj.weaver.ast.Or;

import java.util.List;
import java.util.Optional;

public interface OrdersService {
    void addOrder(Orders order);
    List<Orders> addAllOrders(List<Orders> orders);
    void updateOrder(Orders order);
    void updateOrderStatus(Long orderId, Status status);
    Orders  processCheckout(List<OrderItem> orderItems);
    Orders  getOrderById(Long orderId);

}
