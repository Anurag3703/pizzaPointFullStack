package com.example.fullstack.database.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;


@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {
    @Id
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    @Enumerated(EnumType.STRING)
    private MenuItemCategory category;
    @Enumerated(EnumType.STRING)
    private Size size;
    private Boolean isAvailable;
    private String imageUrl;
    @OneToMany(mappedBy = "menuItem")
    @JsonIdentityReference(alwaysAsId = true)
    private List<CartItem> cartEntries;
    @JsonIgnore
    @OneToMany(mappedBy = "menuItem")
    private List<OrderItem> orderItems;


}


