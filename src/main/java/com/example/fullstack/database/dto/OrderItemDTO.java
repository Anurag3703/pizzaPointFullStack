package com.example.fullstack.database.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderItemDTO {
    private Long id;
    private Long quantity;
    private BigDecimal pricePerItem;
    private String orderId;
    private String menuItemId;
    private List<ExtraDTO> extras;
    private String orderMenuItemName;
}