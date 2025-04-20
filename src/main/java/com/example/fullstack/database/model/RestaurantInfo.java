package com.example.fullstack.database.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
    @NotNull
    private String name;
    @NotNull
    private String address;
    @NotNull
    private String phone;
    @NotNull
    private String email;
    @NotNull
    private String city;
    @NotNull
    private String state;
    private String zip;
    private int rating;
    @NotNull
    private boolean isOpen;
    private String website;
    private String menuUrl;


}
