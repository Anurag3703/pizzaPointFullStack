package com.example.fullstack.config;

import com.example.fullstack.database.model.Orders;
import com.example.fullstack.database.model.Status;
import com.example.fullstack.database.repository.OrderItemRepository;
import com.example.fullstack.database.repository.OrdersRepository;
import com.example.fullstack.security.model.OtpToken;
import com.example.fullstack.security.repository.OtpTokenRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderCleanUpService {

    private final OrdersRepository ordersRepository;
    private final OtpTokenRepository otpTokenRepository;

    private final OrderItemRepository orderItemRepository;

    public OrderCleanUpService(OrdersRepository ordersRepository, OrderItemRepository orderItemRepository
    , OtpTokenRepository otpTokenRepository) {
        this.ordersRepository = ordersRepository;
        this.orderItemRepository = orderItemRepository;
        this.otpTokenRepository = otpTokenRepository;
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
            orderItemRepository.deleteAll(order.getOrderItems());
        }
        ordersRepository.deleteAll(pendingOrders);

        System.out.println(pendingOrders.size() + " pending orders deleted.");

    }

    @Transactional
    @Scheduled(cron = "0 */6 * * * *")
    public void deletePendingOtpToken(){
        LocalDateTime currentTime = LocalDateTime.now();
        List<OtpToken> expiredOtpTokens = otpTokenRepository.findByExpiryTimeBefore(currentTime);

        if(!expiredOtpTokens.isEmpty()) {
            System.out.println(expiredOtpTokens.size() + " expired otp tokens found");
            otpTokenRepository.deleteAll(expiredOtpTokens);
        }else {
            System.out.println("No expired otp tokens found");
        }

    }
}
