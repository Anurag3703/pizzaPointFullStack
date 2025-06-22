package com.example.fullstack.database.dto;

import com.example.fullstack.database.model.MealComponentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealComponentDTO {
    private String id;
    private MealComponentType type;
    private Integer minSelection;
    private Integer maxSelection;
    private List<MenuItemDTO> availableItems;
}
