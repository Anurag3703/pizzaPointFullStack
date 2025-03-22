package com.example.fullstack.database.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemDTO {
    private Long id;
    private Long quantity;
    private BigDecimal pricePerItem;
    private String orderId;
    private String menuItemId;
    private List<ExtraDTO> extras;
}