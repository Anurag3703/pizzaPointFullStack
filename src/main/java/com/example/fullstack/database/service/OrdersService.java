package com.example.fullstack.database.service;

import com.example.fullstack.database.model.Orders;
import org.aspectj.weaver.ast.Or;

import java.util.List;

public interface OrdersService {
    void addOrder(Orders order);
    List<Orders> addAllOrders(List<Orders> orders);
}
