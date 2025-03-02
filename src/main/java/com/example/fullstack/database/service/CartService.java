package com.example.fullstack.database.service;

import com.example.fullstack.database.model.Cart;

public interface CartService {
    public void addItemToCart(String menuItemId, Long quantity);
    void updateItemQuantity(Long cartId, Long quantity);

}
