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



}
