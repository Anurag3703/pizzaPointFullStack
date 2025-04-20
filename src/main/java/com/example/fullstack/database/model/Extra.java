package com.example.fullstack.database.model;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;


@Entity
@JsonIgnoreProperties({"cartItems"})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class Extra {
    @Id
    @NotNull
    private String id;
    @NotNull
    private String name;
    @NotNull
    private BigDecimal price;


}
