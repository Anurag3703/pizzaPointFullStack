package com.example.fullstack.database.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId;
    @ManyToOne(fetch = FetchType.EAGER)

    @JsonIgnore
    private Cart cart;     // CartItem --> Cart --> User

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;    // CartItem --> MenuItem

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "cartItem_extras",
            joinColumns = @JoinColumn(name = "cartItem_id"),
            inverseJoinColumns = @JoinColumn(name = "extra_id")
    )
    @JsonIgnoreProperties("cartItems")
    private List<Extra> extras;     // CartItem --> Extras  ---> (CartItem_Id, Extras_Id)
    private Long quantity;
    private BigDecimal totalPrice;
    private String instruction;

    public BigDecimal getTotalPrice() {
        BigDecimal basePrice = menuItem.getPrice().multiply(BigDecimal.valueOf(quantity));
        BigDecimal extrasPrice = extras.stream()
                .map(Extra::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(quantity));

        return basePrice.add(extrasPrice);
    }



    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + cartItemId +
                ", cart=" + cart +
                ", menuItem=" + menuItem +
                ", extras=" + extras +
                ", quantity=" + quantity +
                ", totalPrice=" + getTotalPrice() +
                '}';
    }
}
