package com.example.fullstack.database.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomMealDTO {
    private String id;
    private MealTemplateDTO template;
    private List<SelectedMealItemDTO> selectedItems;
    private BigDecimal totalPrice;


}
