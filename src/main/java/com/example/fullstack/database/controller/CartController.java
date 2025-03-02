package com.example.fullstack.database.controller;

import com.example.fullstack.database.model.Cart;
import com.example.fullstack.database.service.implementation.CartServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    CartServiceImpl cartServiceImpl;

    public CartController(CartServiceImpl cartServiceImpl) {
        this.cartServiceImpl = cartServiceImpl;
    }

    @PostMapping("/add-to-cart")
    public ResponseEntity<String> addCartItem(@RequestParam String menuItemId, @RequestParam Long quantity) {
        try {
            cartServiceImpl.addItemToCart(menuItemId, quantity);
            return ResponseEntity.ok("Item added to cart successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error adding item to cart: " + e.getMessage());
        }
    }
}
