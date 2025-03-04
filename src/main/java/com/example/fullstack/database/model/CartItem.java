package com.example.fullstack.database.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    private Cart cart;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @ManyToMany
    @JoinTable(
            name = "cartItem_extras",
            joinColumns = @JoinColumn(name = "cartItem_id"),
            inverseJoinColumns = @JoinColumn(name = "extra_id")
    )
    private List<Extra> extras;

    private Long quantity;
    private BigDecimal totalPrice;

    public BigDecimal getTotalPrice() {
        BigDecimal basePrice = menuItem.getPrice().multiply(BigDecimal.valueOf(quantity));
        BigDecimal extrasPrice = extras.stream()
                .map(Extra::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(quantity));

        return basePrice.add(extrasPrice);
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public List<Extra> getExtras() {
        return extras;
    }

    public void setExtras(List<Extra> extras) {
        this.extras = extras;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + id +
                ", cart=" + cart +
                ", menuItem=" + menuItem +
                ", extras=" + extras +
                ", quantity=" + quantity +
                ", totalPrice=" + getTotalPrice() +
                '}';
    }
}
