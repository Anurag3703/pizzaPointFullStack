package com.example.fullstack.database.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.*;


import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Meal {
    @Id
    private String id;
    private String name;
    @OneToMany
    private List<MenuItem> menuItems;
    private BigDecimal price;





    public Meal(String name, List<MenuItem> menuItems) {

        this.name = name;
        this.menuItems = menuItems;
        this.price = calculateTotalPrice(menuItems);
    }

    private BigDecimal calculateTotalPrice(List<MenuItem> menuItems) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (MenuItem item : menuItems) {
            totalPrice = totalPrice.add(item.getPrice());
        }
        return totalPrice;
    }


}
