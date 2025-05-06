package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.Cart;
import com.example.fullstack.database.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUser(User currentUser);

    void deleteByUser(User user);

    List<Cart> findByUserEmail(String email);

    void deleteBySessionId(String sessionId);

    boolean existsByUser(User user);


    // Optional<Cart> findByUser(User user);
}
