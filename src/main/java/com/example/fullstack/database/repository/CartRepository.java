package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.Cart;
import com.example.fullstack.database.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUser(User currentUser);

    void deleteByUser(User user);

    List<Cart> findByUserEmail(String email);

   // Optional<Cart> findByUser(User user);
}
