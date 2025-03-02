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
import java.util.Optional;

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
            cartServiceImpl.addItemToCart(session, menuItemId, quantity);
            return ResponseEntity.ok("Item added to cart successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error adding item to cart: " + e.getMessage());
        }
    }

    @PutMapping("/update/{cartId}")
    public ResponseEntity<String> updateItemQuantity(@PathVariable Long cartId, @RequestParam Long quantity) {
        System.out.println("Updating cart item with ID: " + cartId + ", new quantity: " + quantity);
        try {
            cartServiceImpl.updateItemQuantity(cartId, quantity);
            return new ResponseEntity<>("Cart item updated successfully", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<String> getCartItem(@PathVariable Long id) {
        System.out.println("Fetching cart item with ID: " + id);
        try {
            Optional<Cart> cart = cartServiceImpl.getCart(id);
            return ResponseEntity.ok("Cart item retrieved successfully: " + cart.toString());
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}

