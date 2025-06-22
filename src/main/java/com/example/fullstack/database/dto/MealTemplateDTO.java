package com.example.fullstack.database.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealTemplateDTO {
    private String id;
    private String name;
    private List<MenuItemDTO> menuItems;
    private BigDecimal price;
    private String description;
    private String imageUrl;
    private boolean isActive;
    private String category;
    private List<MealComponentDTO> components;
}
