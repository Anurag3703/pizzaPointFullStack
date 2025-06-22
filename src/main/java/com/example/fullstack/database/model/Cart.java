package com.example.fullstack.database.model;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



@Data
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "cartId")
public class  Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int cartId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CartItem> cartItems = new ArrayList<>();  //List of all cart items

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"carts", "orders", "password", "role"})
    @JsonIgnore
    private User user;    //Cart of a certain user


    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<CustomMeal> customMeals = new ArrayList<>();
    private BigDecimal totalPrice;


    private String sessionId;


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


    public void calculateCartTotalPrice() {
        BigDecimal cartTotalPrice = cartItems.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal deliveryFee = BigDecimal.valueOf(400);
        BigDecimal mealsTotal = customMeals.stream()
                .map(CustomMeal::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalPrice = cartTotalPrice.add(deliveryFee).add(mealsTotal);
    }
    public boolean isEmpty() {
        return (cartItems == null || cartItems.isEmpty()) &&
                (customMeals == null || customMeals.isEmpty());
    }

    // Helper method to get total item count
    public int getTotalItemCount() {
        int itemCount = cartItems.stream()
                .mapToInt(item -> item.getQuantity().intValue())
                .sum();

        int mealCount = customMeals.size(); // Each custom meal counts as 1

        return itemCount + mealCount;
    }

    @Override
    public String toString() {
        return "Cart{" +
                "id=" + cartId +
                ", cartItems=" + cartItems +
                ", user=" + user +
                ", createdAt=" + createdAt +
                ", totalPrice=" + getTotalPrice() +
                '}';
    }
}
