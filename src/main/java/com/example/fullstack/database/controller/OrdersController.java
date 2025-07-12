package com.example.fullstack.database.controller;
import com.example.fullstack.database.dto.OrderDTO;
import com.example.fullstack.database.dto.service.implementation.OrderDTOServiceImpl;
import com.example.fullstack.database.model.*;
import com.example.fullstack.database.service.implementation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.apache.catalina.valves.rewrite.InternalRewriteMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")

public class OrdersController {
    private final OrderItemServiceImpl orderItemServiceImpl;
    private final String adminEmail = "pizzzapointdevelopers@gmial.com";
    OrdersServiceImpl ordersServiceImpl;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;
    private final OrderDTOServiceImpl orderDTOServiceImpl;
    private final WhatsAppService whatsAppService;
    private final AsyncOrderService asyncOrderService;
    public OrdersController(OrdersServiceImpl ordersServiceImpl,
                            OrderItemServiceImpl orderItemServiceImpl,
                            SimpMessagingTemplate messagingTemplate,
                            EmailService emailService,
                            OrderDTOServiceImpl orderDTOServiceImpl,
                            WhatsAppService whatsAppService,AsyncOrderService asyncOrderService

                            ) {
        this.ordersServiceImpl = ordersServiceImpl;
        this.orderItemServiceImpl = orderItemServiceImpl;
        this.messagingTemplate = messagingTemplate;
        this.emailService = emailService;
        this.orderDTOServiceImpl = orderDTOServiceImpl;
        this.whatsAppService = whatsAppService;
        this.asyncOrderService = asyncOrderService;
    }

    @PostMapping
    public String addOrder(@RequestBody Orders order) {
        ordersServiceImpl.addOrder(order);
        return "New order added";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/all")
    public String addAllOrders(@RequestBody List<Orders> orders) {
        ordersServiceImpl.addAllOrders(orders);
        return "All orders added";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable String id, @RequestParam Status status) {
        try{
            Status newStatus = Status.valueOf(status.toUpperCase());
            ordersServiceImpl.updateOrderStatus(id, newStatus);
            Orders updatedOrder = ordersServiceImpl.getOrderById(id);
            OrderDTO orderDTO = orderDTOServiceImpl.convertToDTO(updatedOrder);
//            String toEmail = updatedOrder.getUser().getEmail();
            String statusMessage = getStatusMessage(newStatus);
            System.out.println("Sending to /topic/orders/" + id + ": " + statusMessage);
            messagingTemplate.convertAndSend("/topic/orders/" + id, statusMessage);

            if (newStatus == Status.PLACED) {
                System.out.println("Sending to /topic/admin: New order with ID: " + id);
                messagingTemplate.convertAndSend("/topic/admin", "New order with ID: " + id);
            }
            whatsAppService.sendOrderStatusUpdate(updatedOrder, newStatus);
            if (newStatus == Status.DELIVERED) {
                Orders order = ordersServiceImpl.getOrderById(id);
                String toEmail = order.getUser().getEmail();
                emailService.sendDeliveryEmail(toEmail, orderDTO);
            }

            if((newStatus == Status.CANCELLED)) {
                Orders order = ordersServiceImpl.getOrderById(id);
                String toEmail = order.getUser().getEmail();
                emailService.sendCancellationEmail(toEmail, orderDTO);

            }

            if(newStatus == Status.READY_FOR_PICKUP){
                Orders order = ordersServiceImpl.getOrderById(id);
                String toEmail = order.getUser().getEmail();
                emailService.sendPickupCompletionEmail(toEmail, orderDTO);
            }
            return ResponseEntity.status(HttpStatus.OK).body("Order Status updated to " + newStatus.toString().toLowerCase() + " successfully");
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body("Invalid status: " + status);
        } catch (Exception e) {
             return ResponseEntity.badRequest().body(Map.of("error" , e.getMessage()));
        }
    }



    @PostMapping("/checkout")
    public ResponseEntity<?> deliveryCheckout() {
        try {
            // Call the service to process the checkout
            Orders order = ordersServiceImpl.processCheckoutWithDelivery();
            OrderDTO orderDTO = orderDTOServiceImpl.convertToDTO(order);


            return ResponseEntity.ok(orderDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error during checkout: " + e.getMessage());
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @Operation(summary = "Place an order", description = "Confirm checkout and place an order")
    @PostMapping("/placeorder")
    public ResponseEntity<?> placeOrder(@RequestParam String orderId ,
                                        @RequestParam PaymentMethod paymentMethod,
                                        @RequestParam OrderType orderType,
                                        @RequestParam(required = false) String cardToken,
                                         HttpSession session) {

        try {
            if (paymentMethod == PaymentMethod.CREDIT_CARD && (cardToken == null || cardToken.trim().isEmpty())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Card token is required for card payments"));
            }

            Orders order = ordersServiceImpl.confirmCheckout(orderId, paymentMethod, orderType, cardToken);
            OrderDTO responseDTO = orderDTOServiceImpl.convertToDTO(order);

            // Process all notifications and emails asynchronously
            asyncOrderService.processOrderNotifications(order);
            asyncOrderService.processOrderEmails(order);


            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }


    @PostMapping("/retry-payment")
    public ResponseEntity<?> retryPayment(@RequestParam String orderId, @RequestParam String cardToken) {
        try {
            if (cardToken == null || cardToken.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Card token is required for payment retry"));
            }

            Orders order = ordersServiceImpl.retryPayment(orderId, cardToken);
            OrderDTO responseDTO = orderDTOServiceImpl.convertToDTO(order);

            // Process all notifications and emails asynchronously
            asyncOrderService.processOrderNotifications(order);
            asyncOrderService.processOrderEmails(order);

            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }


    }

    @GetMapping("/history/all-orders/{email}")
    public ResponseEntity<?> getOrderHistoryUser(@PathVariable String email) {
        try {
            List<Orders> orderHistory = ordersServiceImpl.getOrdersByUser(email);
            List<OrderDTO> dto = orderHistory.stream()
                    .map(orderDTOServiceImpl::convertToDTO)
                    .toList();
            return ResponseEntity.ok(dto);
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Error during getOrderHistory: " + e.getMessage());
        }

    }


    @DeleteMapping("/delete-pending-orders")
    public ResponseEntity<?> deletePendingOrders() {
        try{
            ordersServiceImpl.deletePendingOrders();
            return ResponseEntity.ok("Pending orders deleted successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Error during deletePendingOrders: " + e.getMessage());
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new ErrorResponse(errorMessage));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralExceptions(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An unexpected error occurred: " + ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    // You can also handle IllegalArgumentException separately if needed
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    // Error response class for consistent error handling
    public record ErrorResponse(String message) {}
    @PostMapping("/pickup-checkout")
    public ResponseEntity<?> pickupCheckout() {
        try {
            Orders order = ordersServiceImpl.processCheckoutWithPickup();
            OrderDTO orderDTO = orderDTOServiceImpl.convertToDTO(order);
            return ResponseEntity.ok(orderDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("status/placed")
    public ResponseEntity<?> getAllPlacedOrders() {
        try{
            List<Orders> placedOrders = ordersServiceImpl.getAlLPlacedOrders();
            List<OrderDTO> dto = placedOrders.stream()
                    .map(orderDTOServiceImpl::convertToDTO)
                    .toList();
            return ResponseEntity.ok(dto);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("status/delivered")
    public ResponseEntity<?> getAllDeliveredOrders() {
        try{
            List<Orders> deliveredOrders = ordersServiceImpl.getAllDeliveredOrders();
            List<OrderDTO> dto = deliveredOrders.stream()
                    .map(orderDTOServiceImpl::convertToDTO)
                    .toList();
            return ResponseEntity.ok(dto);

        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }



    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("status/cancelled")
    public ResponseEntity<?> getAllCancelledOrders() {
        try{
            List<Orders> cancelledOrders = ordersServiceImpl.getAllCancelledOrders();
            List<OrderDTO> dto = cancelledOrders.stream()
                    .map(orderDTOServiceImpl::convertToDTO)
                    .toList();
            return ResponseEntity.ok(dto);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get/completed-delivered")
    public ResponseEntity<?> getAllCompletedDelivered() {
        try{
            List<Orders> orders = ordersServiceImpl.getAllDeliveredAndCompletedOrders();
            List<OrderDTO> dto = orders.stream().map(orderDTOServiceImpl::convertToDTO).toList();
            return ResponseEntity.ok(dto);

        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }


    @PostMapping("/discount")
    public ResponseEntity<?> confirmCheckoutWithDiscount(@RequestParam String orderId ,
                                                         @RequestParam PaymentMethod paymentMethod,
                                                         @RequestParam OrderType orderType,
                                                         @RequestParam(required = false) String cardToken,
                                                         @RequestParam(required = false) String discountCode) {
        try {
            if (paymentMethod == PaymentMethod.CREDIT_CARD && (cardToken == null || cardToken.trim().isEmpty())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Card token is required for card payments"));
            }

            Orders order = ordersServiceImpl.confirmCheckoutWithDiscount(orderId, paymentMethod, orderType, cardToken,discountCode);
            OrderDTO responseDTO = orderDTOServiceImpl.convertToDTO(order);

            // Process all notifications and emails asynchronously
            asyncOrderService.processOrderNotifications(order);
            asyncOrderService.processOrderEmails(order);

            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }


    }



    @PostMapping("/validate-code")
    public ResponseEntity<?> validateDiscountCode(@RequestParam String discountCode) {
        try{
            ordersServiceImpl.validateDiscountCode(discountCode);
            Map<String, Boolean>  response = new HashMap<>();
            response.put("valid", true);
            return ResponseEntity.ok(response);
        }catch (Exception e){
            Map<String, String>  response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }







}

