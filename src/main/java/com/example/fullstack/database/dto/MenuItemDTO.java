package com.example.fullstack.database.dto;

import com.example.fullstack.database.model.MenuItemCategory;
import com.example.fullstack.database.model.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MenuItemDTO {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private MenuItemCategory category;
    private Size size;
    private Boolean isAvailable;
    private String imageUrl;
    private List<Long> cartEntryIds; // IDs of CartItems
    private List<Long> orderItemIds; // IDs of OrderItems
}