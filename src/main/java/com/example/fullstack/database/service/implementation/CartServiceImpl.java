package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.model.Cart;
import com.example.fullstack.database.model.MenuItem;
import com.example.fullstack.database.model.User;
import com.example.fullstack.database.repository.CartRepository;
import com.example.fullstack.database.repository.MenuItemRepository;
import com.example.fullstack.database.service.CartService;
import com.example.fullstack.database.service.UserService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    CartRepository cartRepository;
    MenuItemRepository menuItemRepository;
    UserService userService;
    public CartServiceImpl(CartRepository cartRepository, MenuItemRepository menuItemRepository, UserService userService) {
        this.cartRepository = cartRepository;
        this.menuItemRepository = menuItemRepository;
        this.userService = userService;
    }

    @Override
    public void addItemToCart(String menuItemId, Long quantity) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu Item Not Found"));

        User user = userService.getCurrentUser();

        Cart cartItem = new Cart();
        cartItem.setMenuItem(menuItem);
        cartItem.setQuantity(quantity);
        cartItem.setUser(user);


        BigDecimal totalPrice = menuItem.getPrice().multiply(BigDecimal.valueOf(quantity));
        cartItem.setTotalPrice(totalPrice);


        cartRepository.save(cartItem);
    }

    @Override
    public void updateItemQuantity(Long cartId, Long quantity) {
        Cart cartItem = cartRepository.findById(Math.toIntExact(cartId))
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than zero");
        }

        // Update the quantity
        BigDecimal totalPrice = cartItem.getPricePerItem().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        cartItem.setQuantity(quantity);
        cartItem.setTotalPrice(totalPrice);
        cartRepository.save(cartItem);




    }

    @Override
    public BigDecimal getCartTotalPrice(Long userId) {

        List<Cart> cartItems = cartRepository.findByUserId(userId);

        // Calculate the total price by summing all cart item total prices
        return cartItems.stream()
                .map(Cart::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


}
