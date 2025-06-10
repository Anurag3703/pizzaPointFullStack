package com.example.fullstack.database.controller;

import com.example.fullstack.config.ModelMapperConfig;
import com.example.fullstack.database.dto.CartDTO;
import com.example.fullstack.database.dto.CartItemDTO;
import com.example.fullstack.database.dto.MenuItemDTO;
import com.example.fullstack.database.dto.ExtraDTO;
import com.example.fullstack.database.dto.service.implementation.CartDTOServiceImpl;
import com.example.fullstack.database.model.Cart;
import com.example.fullstack.database.service.implementation.CartServiceImpl;
import com.example.fullstack.database.service.implementation.UserServiceImpl;
import com.example.fullstack.security.repository.SecurityUserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final SecurityUserRepository securityUserRepository;
    private final ModelMapperConfig modelMapperConfig;
    CartServiceImpl cartServiceImpl;
    UserServiceImpl userServiceImpl;
    ModelMapper modelMapper;
    private final CartDTOServiceImpl cartDTOServiceImpl;

    public CartController(CartServiceImpl cartServiceImpl, UserServiceImpl userServiceImpl, SecurityUserRepository securityUserRepository, ModelMapperConfig modelMapperConfig, ModelMapper modelMapper
    , CartDTOServiceImpl cartDTOServiceImpl) {
        this.cartServiceImpl = cartServiceImpl;
        this.userServiceImpl = userServiceImpl;
        this.securityUserRepository = securityUserRepository;
        this.modelMapperConfig = modelMapperConfig;
        this.modelMapper = modelMapper;
        this.cartDTOServiceImpl = cartDTOServiceImpl;
    }

    @PostMapping("/add-to-cart")
    public ResponseEntity<String> addCartItem(HttpSession session
            , @RequestParam String menuItemId
            , @RequestParam Long quantity
            , @RequestParam(required = false) List<String> extraItemId
            , @RequestParam(required = false) String instructions) {
        try {
            cartServiceImpl.addItemToCart(session, menuItemId, quantity, extraItemId, instructions);
            return ResponseEntity.ok("Item added to cart successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error adding item to cart: " + e.getMessage());
        }
    }

    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<String> updateItemQuantity(@PathVariable Long cartItemId, @RequestParam Long quantity) {
        try {
            cartServiceImpl.updateItemQuantity(cartItemId, quantity);
            return new ResponseEntity<>("Cart item updated successfully", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<CartDTO> getCartItem(@PathVariable Long id) {
        System.out.println("Fetching cart item with ID: " + id);
        try {
            Optional<Cart> cart = cartServiceImpl.getCart(id);
            if (cart.isPresent()) {
                CartDTO cartDTO = cartDTOServiceImpl.convertToDTO(cart.get());
                return ResponseEntity.ok(cartDTO);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get/allCartItems")
    public List<CartDTO> getAllCartItems() {
        List<Cart> carts = cartServiceImpl.getAllCartItems();
        List<CartDTO> cartDTO = carts.stream()
                .map(cartDTOServiceImpl::convertToDTO)
                .collect(Collectors.toList());
        return cartDTO;
    }

    @GetMapping("/user-email")
    public ResponseEntity<List<CartDTO>> getCartByUserEmail(@RequestParam String email) {
        List<Cart> carts = cartServiceImpl.getCartByUserEmail(email);
        List<CartDTO> cartDTOs = carts.stream()
                .map(cartDTOServiceImpl::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(cartDTOs);
    }

}