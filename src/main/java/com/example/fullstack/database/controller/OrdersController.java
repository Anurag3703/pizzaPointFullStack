package com.example.fullstack.database.controller;
import com.example.fullstack.database.dto.OrderDTO;
import com.example.fullstack.database.dto.service.implementation.OrderDTOServiceImpl;
import com.example.fullstack.database.model.*;
import com.example.fullstack.database.service.implementation.EmailService;
import com.example.fullstack.database.service.implementation.OrderItemServiceImpl;
import com.example.fullstack.database.service.implementation.OrdersServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {
    private final OrderItemServiceImpl orderItemServiceImpl;
    OrdersServiceImpl ordersServiceImpl;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;
    private final OrderDTOServiceImpl orderDTOServiceImpl;
    public OrdersController(OrdersServiceImpl ordersServiceImpl,
                            OrderItemServiceImpl orderItemServiceImpl,
                            SimpMessagingTemplate messagingTemplate,
                            EmailService emailService,
                            OrderDTOServiceImpl orderDTOServiceImpl

                            ) {
        this.ordersServiceImpl = ordersServiceImpl;
        this.orderItemServiceImpl = orderItemServiceImpl;
        this.messagingTemplate = messagingTemplate;
        this.emailService = emailService;
        this.orderDTOServiceImpl = orderDTOServiceImpl;
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
    public String updateOrderStatus(@PathVariable String id, @RequestBody Status status) {
        try{
            Status newStatus = Status.valueOf(status.toUpperCase());
            ordersServiceImpl.updateOrderStatus(id, newStatus);
            String statusMessage = getStatusMessage(newStatus);
            messagingTemplate.convertAndSend("/topic/orders/" + id, statusMessage);

            if (newStatus == Status.PLACED) {
                messagingTemplate.convertAndSend("/topic/admin", "New order with ID: " + id);
            }
            if (newStatus == Status.DELIVERED) {
                Orders order = ordersServiceImpl.getOrderById(id);
                String toEmail = order.getUser().getEmail();
                String subject = "Your Order has been Delivered";
                String text = "Hello, your order with ID " + id + " has been delivered successfully. You Can find the Invoice Below";
                emailService.sendEmailWithAttachment(toEmail, subject, text);
            }
            return "Order status updated";
        }catch (IllegalArgumentException e){
            return e.getMessage();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(HttpSession session) {
        try {
            // Call the service to process the checkout
            Orders order = ordersServiceImpl.processCheckout(session);
            OrderDTO orderDTO = orderDTOServiceImpl.convertToDTO(order);


            return ResponseEntity.ok(orderDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error during checkout: " + e.getMessage());
        }
    }

    @GetMapping("/all/orders")
    public ResponseEntity<?> getAllOrders() {
       try{ List<Orders> orders = ordersServiceImpl.getAllOrders();
       List<OrderDTO> dto = orders.stream()
                .map(orderDTOServiceImpl::convertToDTO)
                .collect(Collectors.toList());
       return ResponseEntity.ok(dto);
       } catch (Exception e) {
           return ResponseEntity.badRequest().body("Error during checkout: " + e.getMessage());
       }
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

    @PostMapping("/placeorder")
    public ResponseEntity<?> placeOrder(@RequestParam PaymentMethod paymentMethod,
                                        @RequestParam OrderType orderType,
                                         HttpSession session) {

        try {
            Orders order = ordersServiceImpl.confirmCheckout(paymentMethod, orderType,session);
            OrderDTO responseDTO = orderDTOServiceImpl.convertToDTO(order);
            messagingTemplate.convertAndSend("/topic/orders/" + order.getOrderId(), "Your order has been placed");
            messagingTemplate.convertAndSend("/topic/admin", "New order received: " + order.getOrderId());

            System.out.println("WebSocket message sent for order ID: " + order.getOrderId());


            return ResponseEntity.ok(responseDTO);
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Error during place order: " + e.getMessage());
        }


    }


}

