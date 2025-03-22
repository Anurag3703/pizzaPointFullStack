package com.example.fullstack.database.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItemDTO {
    private Long cartItemId;
    private int cartId;
    private String menuItemId;
    private List<String> extraIds;
    private Long quantity;
    private BigDecimal totalPrice;
    private String instruction;
}