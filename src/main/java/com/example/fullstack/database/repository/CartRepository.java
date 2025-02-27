package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Integer> {

}
