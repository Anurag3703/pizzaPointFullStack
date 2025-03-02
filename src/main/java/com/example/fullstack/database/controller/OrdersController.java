package com.example.fullstack.database.controller;
import com.example.fullstack.database.model.OrderItem;
import com.example.fullstack.database.model.Orders;
import com.example.fullstack.database.model.PaymentMethod;
import com.example.fullstack.database.model.Status;
import com.example.fullstack.database.service.OrdersService;
import com.example.fullstack.database.service.implementation.EmailService;
import com.example.fullstack.database.service.implementation.OrderItemServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {
    private final OrderItemServiceImpl orderItemServiceImpl;
    OrdersService ordersServiceImpl;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;
    public OrdersController(OrdersService ordersServiceImpl, OrderItemServiceImpl orderItemServiceImpl, SimpMessagingTemplate messagingTemplate, EmailService emailService) {
        this.ordersServiceImpl = ordersServiceImpl;
        this.orderItemServiceImpl = orderItemServiceImpl;
        this.messagingTemplate = messagingTemplate;
        this.emailService = emailService;
    }

    @PostMapping
    public String addOrder(@RequestBody Orders order) {
        ordersServiceImpl.addOrder(order);
        return "New order added";
    }

    @PostMapping("/all")
    public String addAllOrders(@RequestBody List<Orders> orders) {
        ordersServiceImpl.addAllOrders(orders);
        return "All orders added";
    }

    @PatchMapping("/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, @RequestBody Status status) {
        try{
            Status newStatus = Status.valueOf(status.toUpperCase());
            ordersServiceImpl.updateOrderStatus(id, newStatus);
            String statusMessage = getStatusMessage(newStatus);
            messagingTemplate.convertAndSend("/topic/orders" + id, statusMessage);
            if (newStatus == Status.DELIVERED) {
                Orders order = ordersServiceImpl.getOrderById(id);
                String toEmail = order.getUser().getEmail();
                String subject = "Your Order has been Delivered";
                String text = "Hello, your order with ID " + id + " has been delivered successfully. You Can find the Invoice Below";
                File attachment = new File("path_to_invoice.pdf");
                emailService.sendEmailWithAttachment(toEmail, subject, text, attachment);
            }
            return "Order status updated";
        }catch (IllegalArgumentException e){
            return e.getMessage();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PatchMapping("/{orderId}/items/{menuItemId}/increase/{quantity}")
    public String increaseItemQuantity(@PathVariable Long orderId,
                                       @PathVariable Long menuItemId,
                                       @PathVariable Integer quantity) {
        orderItemServiceImpl.increaseItemQuantity(orderId, menuItemId, quantity);
        return "Item quantity increased";
    }

    @PatchMapping("/{orderId}/items/{menuItemId}/decrease/{quantity}")
    public String decreaseItemQuantity(@PathVariable Long orderId,
                                       @PathVariable Long menuItemId,
                                       @PathVariable Integer quantity) {
        orderItemServiceImpl.decreaseItemQuantity(orderId, menuItemId, quantity);
        return "Item quantity decreased";
    }

    @PostMapping("/checkout")
    public ResponseEntity<Orders> processCheckout(@RequestBody List<OrderItem> ordersItems, PaymentMethod paymentMethod,String address) {
        Orders order =  ordersServiceImpl.processCheckout(ordersItems,paymentMethod,address);
        return ResponseEntity.ok(order);
    }


    private String getStatusMessage(Status status) {
        return switch (status) {
            case PENDING -> "Pending order";
            case PREPARING -> "Your order is in the Kitchen";
            case READY_FOR_PICKUP -> "Your order is ready for pickup";
            case OUT_FOR_DELIVERY -> "Your order is out for delivery";
            case DELIVERED -> "Your order is delivered";
            case CANCELLED -> "Your order has been cancelled";
            default -> "Unknown order status";
        };
    }


}

