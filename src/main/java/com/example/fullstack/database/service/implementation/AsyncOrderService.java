package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.dto.OrderDTO;
import com.example.fullstack.database.dto.service.implementation.OrderDTOServiceImpl;
import com.example.fullstack.database.model.Orders;
import com.example.fullstack.database.model.Status;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AsyncOrderService {
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;
    private final WhatsAppService whatsAppService;
    private final OrderDTOServiceImpl orderDTOServiceImpl;

    public AsyncOrderService(SimpMessagingTemplate messagingTemplate,
                             EmailService emailService,
                             WhatsAppService whatsAppService,
                             OrderDTOServiceImpl orderDTOServiceImpl) {
        this.messagingTemplate = messagingTemplate;
        this.emailService = emailService;
        this.whatsAppService = whatsAppService;
        this.orderDTOServiceImpl = orderDTOServiceImpl;
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> processOrderNotifications(Orders order) {
        try {
            // Send WebSocket notifications
            messagingTemplate.convertAndSend("/topic/orders/" + order.getOrderId(),
                    "Your order has been placed successfully!");
            messagingTemplate.convertAndSend("/topic/admin",
                    "New order received: " + order.getOrderId());

            // Send WhatsApp notification if order is placed
            if (order.getStatus() == Status.PLACED) {
                whatsAppService.sendNewOrderNotification(order);
            }

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            // Log the error but don't throw it to avoid affecting the main flow
            System.err.println("Error in processOrderNotifications: " + e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> processOrderEmails(Orders order) {
        try {
            if (order.getStatus() == Status.PLACED) {
                OrderDTO orderDTO = orderDTOServiceImpl.convertToDTO(order);
                emailService.sendNewOrderAdminEmail(orderDTO);
            }
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            // Log the error but don't throw it to avoid affecting the main flow
            System.err.println("Error in processOrderEmails: " + e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }
}
