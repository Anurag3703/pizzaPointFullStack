package com.example.fullstack.database.dto;

import com.example.fullstack.database.model.MenuItemCategory;
import com.example.fullstack.database.model.Size;
import lombok.Data;

import java.math.BigDecimal;

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

}