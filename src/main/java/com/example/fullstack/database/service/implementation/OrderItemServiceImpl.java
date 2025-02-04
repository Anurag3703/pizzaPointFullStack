package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.model.OrderItem;
import com.example.fullstack.database.repository.OrderItemRepository;
import com.example.fullstack.database.service.OrderItemService;
import org.springframework.stereotype.Service;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    OrderItemRepository orderItemRepository;
    public OrderItemServiceImpl(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }
    @Override
    public void createOrderItem(OrderItem orderItem) {
        orderItemRepository.save(orderItem);

    }
}
