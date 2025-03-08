package com.example.fullstack.database.model;

import jakarta.persistence.*;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Entity
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Orders {

    @Id
    @Column(length = 36)
    private String orderId;
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

    @PrePersist
    public void generateId() {
        if (orderId == null) {
            orderId = UUID.randomUUID().toString();
        }
    }

    public BigDecimal getTotalPrice() {
        // Calculate total price dynamically based on order items
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            total = total.add(item.getTotalPrice());
        }
        return total;
    }

}
