package com.example.fullstack.database.model;


import jakarta.persistence.*;
import lombok.*;


@Entity
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false)
    private String name;
    private String address;
    private String phone;
    private String email;
    private String city;
    private String state;
    private String zip;
    private int rating;
    private boolean isOpen;
    private String website;
    private String menuUrl;


}
