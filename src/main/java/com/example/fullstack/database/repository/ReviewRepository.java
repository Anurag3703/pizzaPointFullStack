package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
