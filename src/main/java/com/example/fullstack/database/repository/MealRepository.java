package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.MealTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MealRepository extends JpaRepository<MealTemplate, String> {


}
