package com.example.fullstack.database.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CartDTO {
    private int cartId;
    private List<CartItemDTO> cartItems;
    private Long userId;
    private LocalDateTime createdAt;
    private BigDecimal totalPrice;
    private String sessionId;
}