package com.example.fullstack.database.model;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private Long quantity;
    private BigDecimal totalPrice;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private LocalDateTime createdAt;


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Cart() {
    }


    public Cart(int id, Long quantity,  MenuItem menuItem,User user, LocalDateTime createdAt) {
        this.id = id;
        this.quantity = quantity;
        this.user = user;
        this.menuItem = menuItem;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }





    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public BigDecimal getPricePerItem() {
        return menuItem.getPrice();
    }

    public BigDecimal getTotalPrice() {
        return menuItem.getPrice().multiply(new BigDecimal(quantity));
    }

    @Override
    public String toString() {
        return "Cart{" +
                "id=" + id +
                ", quantity=" + quantity +
                ", totalPrice=" + totalPrice +
                ", menuItem=" + menuItem +
                ", user=" + user +
                ", createdAt=" + createdAt +
                '}';
    }
}
