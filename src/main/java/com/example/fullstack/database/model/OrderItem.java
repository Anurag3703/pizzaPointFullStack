package com.example.fullstack.database.model;

import jakarta.persistence.*;
import lombok.Generated;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long quantity;
    private BigDecimal pricePerItem;
    private BigDecimal totalPrice;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menuItem_id",nullable = false)
    private MenuItem menuItem;

    public OrderItem() {
    }

    public OrderItem(Long id, Long quantity, BigDecimal pricePerItem, BigDecimal totalPrice, Orders order, MenuItem menuItem) {
        this.id = id;
        this.quantity = quantity;
        this.pricePerItem = pricePerItem;
        this.totalPrice = totalPrice;
        this.order = order;
        this.menuItem = menuItem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPricePerItem() {
        return pricePerItem;
    }

    public void setPricePerItem(BigDecimal pricePerItem) {
        this.pricePerItem = pricePerItem;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Orders getOrder() {
        return order;
    }

    public void setOrder(Orders order) {
        this.order = order;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return id == orderItem.id && Objects.equals(quantity, orderItem.quantity) && Objects.equals(pricePerItem, orderItem.pricePerItem) && Objects.equals(totalPrice, orderItem.totalPrice) && Objects.equals(order, orderItem.order) && Objects.equals(menuItem, orderItem.menuItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, quantity, pricePerItem, totalPrice, order, menuItem);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", quantity=" + quantity +
                ", pricePerItem=" + pricePerItem +
                ", totalPrice=" + totalPrice +
                ", order=" + order +
                ", menuItem=" + menuItem +
                '}';
    }
}
