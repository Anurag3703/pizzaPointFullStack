package com.example.fullstack.database.service;

import com.example.fullstack.database.model.Meal;
import com.example.fullstack.database.model.MenuItem;

import java.util.List;

public interface MealService {
    void addMeal(Meal meal);
    void deleteMeal(Meal meal);
    void updateMeal(Meal meal);
    List<Meal> getMeals();
    Meal createServiceMeal(List<MenuItem> selectedItems);

}
