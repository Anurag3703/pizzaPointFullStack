package com.example.fullstack.database.controller;
import com.example.fullstack.database.model.CartItem;
import com.example.fullstack.database.service.implementation.CartItemServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/cartItem")
public class CartItemController {

    private final CartItemServiceImpl cartItemServiceImpl;
    public CartItemController(CartItemServiceImpl cartItemServiceImpl) {
        this.cartItemServiceImpl = cartItemServiceImpl;
    }


    @GetMapping("/all")
    public List<CartItem> getAll() {
        return cartItemServiceImpl.getAllCartItems();
    }
}
