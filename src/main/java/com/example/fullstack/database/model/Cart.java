package com.example.fullstack.database.model;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)

    private List<CartItem> cartItems = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"carts", "orders", "password", "role"})
    private User user;
    private LocalDateTime createdAt;
    private BigDecimal totalPrice;


    @PrePersist
    protected void onCreate() {

        this.createdAt = LocalDateTime.now();
    }

    public Cart() {
    }


    public Cart(int id,  List<CartItem> cartItems, User user, LocalDateTime createdAt, BigDecimal totalPrice) {
        this.id = id;
        this.cartItems = cartItems;
        this.user = user;
        this.createdAt = createdAt;
        this.totalPrice = totalPrice;
    }

    public int getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }



    public void setId(int id) {
        this.id = id;
    }



    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
//    public BigDecimal getPricePerItem() {
//        return menuItem.getPrice();
//    }


    public BigDecimal getTotalPrice() {
         return cartItems.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    @Override
    public String toString() {
        return "Cart{" +
                "id=" + id +
                ", cartItems=" + cartItems +
                ", user=" + user +
                ", createdAt=" + createdAt +
                ", totalPrice=" + getTotalPrice() +
                '}';
    }
}
