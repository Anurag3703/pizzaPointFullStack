package com.example.fullstack.database.model;

import com.example.fullstack.security.model.UserSecurity;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Table(name = "users")
@Entity
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NotNull
    private String name;
    @NotNull
    private String address;
    @NotNull
    private String phone;
    @NotNull
    private String email;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_security_id")
    private UserSecurity userSecurity;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Orders> orders = new ArrayList<>();
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Review> reviews;

    private boolean isAnonymous = false;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Address> addresses;
    public User(String name, String address, String phone, String email,boolean isAnonymous) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.isAnonymous = isAnonymous;

    }



}
