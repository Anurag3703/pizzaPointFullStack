package com.example.fullstack.database.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;


import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Entity
public class Meal {
    @Id
    private String id;
    private String name;
    @OneToMany
    private List<MenuItem> menuItems;
    private BigDecimal price;



    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

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
    public Meal() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
        this.price = calculateTotalPrice(menuItems);
    }

    @Override
    public String toString() {
        return "Meal{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", menuItems=" + menuItems +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Meal meal = (Meal) o;
        return Objects.equals(id, meal.id) && Objects.equals(name, meal.name) && Objects.equals(menuItems, meal.menuItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, menuItems);
    }
}
