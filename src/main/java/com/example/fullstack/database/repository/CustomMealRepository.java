package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.CustomMeal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomMealRepository extends JpaRepository<CustomMeal, String > {
}
