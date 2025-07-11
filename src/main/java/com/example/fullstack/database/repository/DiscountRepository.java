package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.Discount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiscountRepository extends JpaRepository<Discount, Integer> {
    Optional<Discount> findByDiscountCodeAndActiveTrue(String code);
}
