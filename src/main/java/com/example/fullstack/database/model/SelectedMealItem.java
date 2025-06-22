package com.example.fullstack.database.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectedMealItem {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    private MenuItem menuItem;

    @ManyToOne
    private CustomMeal customMeal;

    private Integer quantity = 1;

    @ManyToMany
    private List<Extra> extras = new ArrayList<>();

    private String specialInstructions;

    @Transient
    public BigDecimal getPrice() {
        BigDecimal basePrice = menuItem.getPrice();
        BigDecimal extrasPrice = extras.stream()
                .map(Extra::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return basePrice.add(extrasPrice).multiply(BigDecimal.valueOf(quantity));
    }
}
