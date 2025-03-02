package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Integer> {
        List<Cart> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
