package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.model.MealTemplate;
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
    public void addMeal(MealTemplate meal) {
        mealRepository.save(meal);
    }

    @Override
    public void deleteMeal(MealTemplate meal) {

    }

    @Override
    public void updateMeal(MealTemplate meal) {

    }

    @Override
    public List<MealTemplate> getMeals() {
        return List.of();
    }

    @Override
    public MealTemplate createServiceMeal(List<MenuItem> selectedItems) {
        return new MealTemplate();
    }


}
