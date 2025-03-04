package com.example.fullstack.database.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Entity
public class MenuItem {
    @Id
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    @Enumerated(EnumType.STRING)
    private MenuItemCategory category;
    @Enumerated(EnumType.STRING)
    private Size size;
    private Boolean isAvailable;
    private String imageUrl;
    @OneToMany(mappedBy = "menuItem")
    private List<CartItem> cartEntries;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }


    @OneToMany(mappedBy = "menuItem")
    private List<OrderItem> orderItems;



    public MenuItem() {
    }
    public MenuItem(String name, String description, BigDecimal price, MenuItemCategory category, Size size, Boolean isAvailable, String imageUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.size = size;
        this.isAvailable = isAvailable;
        this.imageUrl = imageUrl;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public MenuItemCategory getCategory() {
        return category;
    }

    public void setCategory(MenuItemCategory category) {
        this.category = category;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", category=" + category +
                ", size=" + size +
                ", isAvailable=" + isAvailable +
                ", imageUrl='" + imageUrl + '\'' +
                ", cartEntries=" + cartEntries +
                ", orderItems=" + orderItems +
                '}';
    }

    public List<CartItem> getCartEntries() {
        return cartEntries;
    }

    public void setCartEntries(List<CartItem> cartEntries) {
        this.cartEntries = cartEntries;
    }
}


