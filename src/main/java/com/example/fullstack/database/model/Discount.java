package com.example.fullstack.database.model;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "discounts")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter

public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(unique = true, nullable = false)
    private String discountCode;

    private BigDecimal discountValue;

    private BigDecimal minimumOrderAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    private LocalDateTime validFrom;
    private LocalDateTime validUntil;

    private Integer maxUses;

    private Integer currentUses = 0;

    private boolean active = true;

    @Enumerated(EnumType.STRING)
    private OrderType applicableOrderType;


    @Transient
    public boolean isExpired() {
        return validUntil != null && validUntil.isBefore(LocalDateTime.now());
    }

    @Transient
    public boolean isValidNow() {
        return (validFrom == null || validFrom.isBefore(LocalDateTime.now())) &&
                (validUntil == null || validUntil.isAfter(LocalDateTime.now())) &&
                active && (maxUses == null || currentUses < maxUses);
    }

    @PrePersist
    @PreUpdate
    public void validate() {
        if (discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Discount value must be positive");
        }
        if (minimumOrderAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Minimum order amount cannot be negative");
        }
        if(minimumOrderAmount.compareTo(new BigDecimal("3500")) < 0){
            throw new RuntimeException(
                    "Minimum order amount for Discount is 3500 HUF. Please add more items to reach the minimum."
            );
        }
        if (validFrom != null && validUntil != null && validUntil.isBefore(validFrom)) {
            throw new IllegalArgumentException("Valid until date cannot be before valid from date");
        }
    }



}
