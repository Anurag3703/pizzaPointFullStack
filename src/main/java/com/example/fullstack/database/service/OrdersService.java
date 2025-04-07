package com.example.fullstack.database.service;

import com.example.fullstack.database.model.*;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.aspectj.weaver.ast.Or;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdersService {
    void addOrder(Orders order);
    List<Orders> addAllOrders(List<Orders> orders);
    void updateOrder(Orders order);
    void updateOrderStatus(String orderId, Status status);
    Orders  processCheckout(HttpSession session);
    List<Orders> getAllOrders();


    Orders  getOrderById(String orderId);
    Orders confirmCheckout(PaymentMethod paymentMethod, OrderType orderType,HttpSession session);


}
