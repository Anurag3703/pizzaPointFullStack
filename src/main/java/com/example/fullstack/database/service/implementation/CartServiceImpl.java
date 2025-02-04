package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.model.Cart;
import com.example.fullstack.database.repository.CartRepository;
import com.example.fullstack.database.service.CartService;
import jakarta.persistence.OneToMany;
import org.springframework.stereotype.Service;

@Service
public class CartServiceImpl implements CartService {

    CartRepository cartRepository;
    public CartServiceImpl(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }
    @Override
    public void addCart(Cart cart) {
        cartRepository.save(cart);

    }
}
