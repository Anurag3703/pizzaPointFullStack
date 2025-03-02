package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.model.Cart;
import com.example.fullstack.database.model.MenuItem;
import com.example.fullstack.database.model.User;
import com.example.fullstack.database.repository.CartRepository;
import com.example.fullstack.database.repository.MenuItemRepository;
import com.example.fullstack.database.service.CartService;
import com.example.fullstack.database.service.UserService;
import jakarta.persistence.OneToMany;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
        cartItem.setQuantity(quantity);

        // Recalculate the total price for the cart item if necessary (if your total price logic is in the CartItem)
        cartRepository.save(cartItem);

    }
}
