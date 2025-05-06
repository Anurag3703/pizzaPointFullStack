package com.example.fullstack.config;


import com.example.fullstack.database.model.OrderItem;
import com.example.fullstack.database.model.Orders;
import com.example.fullstack.database.model.Status;
import com.example.fullstack.database.repository.OrderItemRepository;
import com.example.fullstack.database.repository.OrdersRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.transform.Source;
import java.util.List;

@Service
public class OrderCleanUpService {

    private final OrdersRepository ordersRepository;

    private final OrderItemRepository orderItemRepository;

    public OrderCleanUpService(OrdersRepository ordersRepository, OrderItemRepository orderItemRepository) {
        this.ordersRepository = ordersRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    @Scheduled(cron = "0 0 * * * ?")
    public void deletePendingOrders() {
        List<Orders> pendingOrders = ordersRepository.findByStatus(Status.PENDING);

        if(pendingOrders.isEmpty()) {
            System.out.println("No pending orders found");
            return;
        }

        for(Orders order : pendingOrders) {
            for(OrderItem orderItem : order.getOrderItems()) {
                orderItemRepository.delete(orderItem);
            }
        }
        ordersRepository.deleteAll(pendingOrders);

        System.out.println(pendingOrders.size() + " pending orders deleted.");

    }
}
