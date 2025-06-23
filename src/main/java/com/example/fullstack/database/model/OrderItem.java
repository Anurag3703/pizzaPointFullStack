package com.example.fullstack.database.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long quantity;
    private BigDecimal pricePerItem;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menuItem_id")
    private MenuItem menuItem;

    @ManyToOne
    @JoinColumn(name = "custom_meal_id")
    private CustomMeal customMeal;

    @ManyToMany
    @JoinTable(
            name = "order_item_extras", // Join table name
            joinColumns = @JoinColumn(name = "order_item_id"),
            inverseJoinColumns = @JoinColumn(name = "extra_id")
    )
    private List<Extra> extras = new ArrayList<>();


    public BigDecimal getTotalPrice() {
        return pricePerItem.multiply(new BigDecimal(quantity));
    }


}
