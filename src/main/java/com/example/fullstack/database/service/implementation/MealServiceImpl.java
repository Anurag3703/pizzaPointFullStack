package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.model.Meal;
import com.example.fullstack.database.model.MenuItem;
import com.example.fullstack.database.repository.MealRepository;
import com.example.fullstack.database.service.MealService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MealServiceImpl implements MealService {
    private final MealRepository mealRepository;
    public MealServiceImpl(MealRepository mealRepository) {
        this.mealRepository = mealRepository;
    }
    @Override
    public void addMeal(Meal meal) {
        mealRepository.save(meal);
    }

    @Override
    public void deleteMeal(Meal meal) {

    }

    @Override
    public void updateMeal(Meal meal) {

    }

    @Override
    public List<Meal> getMeals() {
        return List.of();
    }

    @Override
    public Meal createServiceMeal(List<MenuItem> selectedItems) {
        return new Meal("Custom Combo", selectedItems);
    }


}
