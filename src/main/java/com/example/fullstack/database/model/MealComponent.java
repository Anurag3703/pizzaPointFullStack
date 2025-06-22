package com.example.fullstack.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealComponent {

    @Id
    private String id;
    @NotNull
    @Enumerated(EnumType.STRING)
    private MealComponentType type; // PIZZA, DRINK, SIDE, etc.

    @NotNull
    private Integer minSelection; // Minimum items required from this component

    private Integer maxSelection; // Maximum items allowed (null for unlimited)

    @ManyToMany
    @JoinTable(
            name = "component_menu_items",
            joinColumns = @JoinColumn(name = "component_id"),
            inverseJoinColumns = @JoinColumn(name = "menu_item_id")
    )
    private List<MenuItem> availableItems = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "meal_template_id")
    private MealTemplate mealTemplate;
}

