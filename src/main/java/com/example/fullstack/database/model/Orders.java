package com.example.fullstack.database.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private Long orderSequence;
    private BigDecimal totalPrice;
    @Enumerated(EnumType.STRING)
    private Status status;
    private LocalDate date;
    @ManyToOne
    @JoinColumn(name = "address_id", nullable = true)
    private Address address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    private BigDecimal deliveryFee;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<OrderItem> orderItems = new ArrayList<>();
    private BigDecimal totalCartAmount;

    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    private BigDecimal serviceFee = BigDecimal.valueOf(150);


    // Initialize to ZERO, not 50
    private BigDecimal bottleDepositFee = BigDecimal.ZERO;

    // ADDED: Constant for per-bottle deposit fee
    private static final BigDecimal PER_BOTTLE_DEPOSIT_FEE = BigDecimal.valueOf(50);

    private String transactionId;

    @PrePersist
    public void generateId() {
        if (orderId == null) {
            orderId = UUID.randomUUID().toString();
        }
    }

    //  This method should return the per-bottle fee, not the total
    public BigDecimal getPerBottleDepositFee() {
        return PER_BOTTLE_DEPOSIT_FEE;
    }

    @Transient
    public BigDecimal getTotalCartAmount() {
        return orderItems.stream()
                .map(item -> item.getPricePerItem().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transient
    public BigDecimal getTotalBottleDepositFee() {
        long drinkItemCount = orderItems.stream()
                .mapToLong(item -> {
                    // Handle regular menu items
                    if (item.getMenuItem() != null &&
                            item.getMenuItem().getCategory() == MenuItemCategory.DRINKS) {
                        return item.getQuantity();
                    }
                    // Handle custom meals - you can add logic here if custom meals
                    // can contain drinks that should incur bottle deposit fees
                    else if (item.getCustomMeal() != null) {
                        // For now, return 0, but you could check custom meal ingredients
                        // if they can contain drinks that need bottle deposits
                        return 0L;
                    }
                    return 0L;
                })
                .sum();

        return PER_BOTTLE_DEPOSIT_FEE.multiply(BigDecimal.valueOf(drinkItemCount));
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Transient
    public String getFormattedOrderNumber() {
        if (orderSequence == null) {
            return null;
        }
        return "#Order-" + String.format("%04d", orderSequence);
    }
}