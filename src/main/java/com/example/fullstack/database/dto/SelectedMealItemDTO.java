package com.example.fullstack.database.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelectedMealItemDTO {
    private String menuItemId;
    private Integer quantity;
    private String specialInstructions;
}
