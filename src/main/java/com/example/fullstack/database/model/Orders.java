package com.example.fullstack.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal totalPrice;
    @Enumerated(EnumType.STRING)
    private Status status;
    private LocalDate date;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")

    private User user;
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;



    // No-args constructor
    public Orders() {
    }

    // All-args constructor
    public Orders(Long id, BigDecimal totalPrice, Status status, LocalDate date, String address,
                  LocalDateTime createdAt, LocalDateTime updatedAt, User user, PaymentMethod paymentMethod) {
        this.id = id;
        this.totalPrice = totalPrice;
        this.status = status;
        this.date = date;
        this.address = address;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.user = user;
        this.paymentMethod = paymentMethod;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    // toString method
    @Override
    public String toString() {
        return "Orders{" +
                "id=" + id +
                ", totalPrice=" + totalPrice +
                ", status=" + status +
                ", date=" + date +
                ", address='" + address + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", user=" + user +
                ", paymentMethod=" + paymentMethod +
                '}';
    }

    // equals method
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Orders orders = (Orders) o;
        return Objects.equals(id, orders.id) &&
                Objects.equals(totalPrice, orders.totalPrice) &&
                status == orders.status &&
                Objects.equals(date, orders.date) &&
                Objects.equals(address, orders.address) &&
                Objects.equals(createdAt, orders.createdAt) &&
                Objects.equals(updatedAt, orders.updatedAt) &&
                Objects.equals(user, orders.user) &&
                paymentMethod == orders.paymentMethod;
    }

    // hashCode method
    @Override
    public int hashCode() {
        return Objects.hash(id, totalPrice, status, date, address, createdAt, updatedAt, user, paymentMethod);
    }
}
