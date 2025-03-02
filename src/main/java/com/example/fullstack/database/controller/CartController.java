package com.example.fullstack.database.controller;

import com.example.fullstack.database.model.Cart;
import com.example.fullstack.database.model.User;
import com.example.fullstack.database.service.implementation.CartServiceImpl;
import com.example.fullstack.database.service.implementation.UserServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    CartServiceImpl cartServiceImpl;
    UserServiceImpl userServiceImpl;

    public CartController(CartServiceImpl cartServiceImpl, UserServiceImpl userServiceImpl) {
        this.cartServiceImpl = cartServiceImpl;
        this.userServiceImpl = userServiceImpl;
    }

    @PostMapping("/add-to-cart")
    public ResponseEntity<String> addCartItem(HttpSession session, @RequestParam String menuItemId, @RequestParam Long quantity) {
        try {
            cartServiceImpl.addItemToCart(session,menuItemId, quantity);
            return ResponseEntity.ok("Item added to cart successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error adding item to cart: " + e.getMessage());
        }
    }


}

