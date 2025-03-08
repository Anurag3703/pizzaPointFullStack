package com.example.fullstack.database.service;

import com.example.fullstack.database.model.CartItem;
import com.example.fullstack.database.model.User;

import java.util.List;

public interface CartItemService {

    List<CartItem> getAllCartItems();

}
