package com.example.fullstack.database.service;

import com.example.fullstack.database.model.MealTemplate;
import com.example.fullstack.database.model.MenuItem;

import java.util.List;

public interface MealService {
    void addMeal(MealTemplate meal);
    void deleteMeal(MealTemplate meal);
    void updateMeal(MealTemplate meal);
    List<MealTemplate> getMeals();
    MealTemplate createServiceMeal(List<MenuItem> selectedItems);

}
