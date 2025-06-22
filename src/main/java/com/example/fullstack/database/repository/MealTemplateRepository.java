package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.MealTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealTemplateRepository extends JpaRepository<MealTemplate, String> {
}
