package com.example.fullstack.database.model;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MealTemplate {
    @Id
    private String id;
    private String name;
    @OneToMany
    @ToString.Exclude
    private List<MenuItem> menuItems;
    private BigDecimal price;
    private String description;
    private String imageUrl;
    private boolean isActive = true;
    private String category;
    @OneToMany(mappedBy = "mealTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<MealComponent> components = new ArrayList<>();




//    public MealTemplate(String name, List<MenuItem> menuItems) {
//
//        this.name = name;
//        this.menuItems = menuItems;
//        this.price = calculateTotalPrice(menuItems);
//    }

//    private BigDecimal calculateTotalPrice(List<MenuItem> menuItems) {
//        BigDecimal totalPrice = BigDecimal.ZERO;
//        for (MenuItem item : menuItems) {
//            totalPrice = totalPrice.add(item.getPrice());
//        }
//        return totalPrice;
//    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        MealTemplate that = (MealTemplate) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
