package com.example.fullstack.database.model;


import jakarta.persistence.*;
import lombok.*;

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
    private boolean isSelected;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
