package com.example.fullstack.database.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;
    private String buildingName;
    private String floor;
    private String intercom;
    private String apartmentNo;
    private String street;
    private String otherInstructions;
    private boolean selected;
    private boolean recent;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private LocalDateTime createdDateTime;

    @PrePersist
    protected void onCreate() {
        createdDateTime = LocalDateTime.now();
    }

}
