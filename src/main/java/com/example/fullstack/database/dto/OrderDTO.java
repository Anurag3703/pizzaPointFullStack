package com.example.fullstack.database.dto;

import com.example.fullstack.database.model.OrderType;
import com.example.fullstack.database.model.PaymentMethod;
import com.example.fullstack.database.model.Status;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDTO {
    private String orderId;
    private String orderSequence;
    private BigDecimal totalPrice;
    private Status status;
    private LocalDate date;
    private AddressDTO address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserDTO user;
    private PaymentMethod paymentMethod;
    private List<OrderItemDTO> orderItems;
    private OrderType orderType;
    private BigDecimal deliveryFee;
    private BigDecimal serviceFee;
    private BigDecimal bottleDepositFee;
    private BigDecimal totalCartAmount;
}