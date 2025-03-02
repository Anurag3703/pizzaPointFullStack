package com.example.fullstack.database.service;

import com.example.fullstack.database.model.Cart;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;

public interface CartService {
    public void addItemToCart(HttpSession session, String menuItemId, Long quantity);
    void updateItemQuantity(Long cartId, Long quantity);

    BigDecimal getCartTotalPrice(HttpSession session);
}
