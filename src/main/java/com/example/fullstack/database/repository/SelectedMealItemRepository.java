package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.SelectedMealItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SelectedMealItemRepository extends JpaRepository<SelectedMealItem, String> {
}
