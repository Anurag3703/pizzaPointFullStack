package com.example.fullstack.database.service;

import com.example.fullstack.database.dto.SelectedMealItemDTO;
import com.example.fullstack.database.model.Cart;
import com.example.fullstack.database.model.CartItem;
import com.example.fullstack.database.model.User;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CartService {
    public void addItemToCart(HttpSession session, String menuItemId, Long quantity,List<String> extraItemId, String instructions);
    void updateItemQuantity(Long cartId, Long quantity);
    Optional<Cart> getCart(Long cartId);

    BigDecimal getCartTotalPrice(HttpSession session);
    List<Cart> getAllCartItems();
    List<Cart> getCartByUserEmail(String email);
    void transferGuestCartToUser(HttpSession session, User user);
    BigDecimal calculateCartItemTotalPrice(CartItem cartItem);
    BigDecimal calculateCartTotalPrice(Cart cart);
    void addMealToCart(String mealTemplateId, List<SelectedMealItemDTO> selectedItems);
    void updateMealQuantity(String customMealId, Integer quantity);

    Cart getCurrentUserCart();
    //Cart getCartByUser(User user);
}
