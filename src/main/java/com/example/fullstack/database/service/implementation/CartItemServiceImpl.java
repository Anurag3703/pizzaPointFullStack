package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.model.CartItem;
import com.example.fullstack.database.model.User;
import com.example.fullstack.database.repository.CartItemRepository;
import com.example.fullstack.database.service.CartItemService;
import com.example.fullstack.database.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CartItemServiceImpl implements CartItemService {
    private final CartItemRepository cartItemRepository;
    private final UserService userService;
    public CartItemServiceImpl(CartItemRepository cartItemRepository, UserService userService) {
        this.cartItemRepository = cartItemRepository;
        this.userService = userService;
    }


    @Override
    public List<CartItem> getAllCartItems() {
        User currentUser = userService.getCurrentUser();
        return cartItemRepository.findByCart_User(currentUser);
    }
}
