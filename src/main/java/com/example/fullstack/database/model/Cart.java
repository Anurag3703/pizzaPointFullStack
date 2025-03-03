package com.example.fullstack.database.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
    @JsonIgnoreProperties({"orderItems", "cartEntries"})
    private MenuItem menuItem;
    @ManyToMany
    @JoinTable(
            name = "cart_extras",
            joinColumns = @JoinColumn(name = "cart_id"),
            inverseJoinColumns = @JoinColumn(name = "extra_id")
    )
    private List<Extra> extras;



    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"carts", "orders", "password", "role"})
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

        BigDecimal basePrice = menuItem.getPrice().multiply(new BigDecimal(quantity));
        BigDecimal extrasPrice = extras.stream()
                .map(Extra::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(new BigDecimal(quantity));
        return basePrice.add(extrasPrice);
    }

    public List<Extra> getExtras() {
        return extras;
    }

    public void setExtras(List<Extra> extras) {
        this.extras = extras;
    }

    @Override
    public String toString() {
        return "Cart{" +
                "id=" + id +
                ", quantity=" + quantity +
                ", totalPrice=" + totalPrice +
                ", menuItem=" + menuItem +
                ", extras=" + extras +
                ", user=" + user +
                ", createdAt=" + createdAt +
                '}';
    }
}
