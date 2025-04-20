package com.example.fullstack.database.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {
    @Id
    private String id;
    @NotNull
    private String name;
    private String description;
    @NotNull
    private BigDecimal price;
    @Enumerated(EnumType.STRING)
    @NotNull
    private MenuItemCategory category;
    @Enumerated(EnumType.STRING)
    private Size size;
    @NotNull
    private Boolean isAvailable;
    @NotNull
    private String imageUrl;
    @OneToMany(mappedBy = "menuItem",cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonIdentityReference(alwaysAsId = true)
    private List<CartItem> cartEntries =  new ArrayList<>();
    @JsonIgnore
    @OneToMany(mappedBy = "menuItem",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();


}


