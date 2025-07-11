package com.example.fullstack.database.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "discount_usages")
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DiscountUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "discount_id", nullable = false)
    private Discount discount;


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true) // Nullable to handle deleted users
    private User user;

    @Column(nullable = false)
    private String userPhone; // Phone number to track user even if account is deleted

    @Column(nullable = false)
    private String orderId; // Associated order

    @Column(nullable = false)
    private LocalDateTime usedAt;

}
