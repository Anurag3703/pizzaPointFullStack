package com.example.fullstack.database.controller;

import com.example.fullstack.database.model.OrderItem;
import com.example.fullstack.database.service.implementation.OrderItemServiceImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orderItems")
public class OrderItemController {

    private final OrderItemServiceImpl orderItemServiceImpl;
    OrderItemServiceImpl orderItemService;
    public OrderItemController(OrderItemServiceImpl orderItemService, OrderItemServiceImpl orderItemServiceImpl) {
        this.orderItemService = orderItemService;
        this.orderItemServiceImpl = orderItemServiceImpl;
    }

    @PostMapping
    public String addOrderItem(@RequestBody OrderItem orderItem) {
        orderItemServiceImpl.createOrderItem(orderItem);
        return "Order item added";
    }
}
