package com.example.fullstack.database.service;

import com.example.fullstack.database.model.Cart;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CartService {
    public void addItemToCart(HttpSession session, String menuItemId, Long quantity,List<String> extraItemId);
    void updateItemQuantity(Long cartId, Long quantity);
    Optional<Cart> getCart(Long cartId);

    BigDecimal getCartTotalPrice(HttpSession session);
    List<Cart> getAllCartItems();
    List<Cart> getCartByUserEmail(String email);
}
