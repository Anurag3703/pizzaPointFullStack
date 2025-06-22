package com.example.fullstack.database.controller;

import com.example.fullstack.database.model.MealTemplate;
import com.example.fullstack.database.model.MenuItem;
import com.example.fullstack.database.service.implementation.MealServiceImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/meals")
public class MealController {


    private final MealServiceImpl mealServiceImpl;

    public MealController( MealServiceImpl mealServiceImpl) {

        this.mealServiceImpl = mealServiceImpl;
    }

    @PostMapping("/create")
    public MealTemplate createMeal(@RequestBody List<MenuItem> selectedMenuItems) {
        return mealServiceImpl.createServiceMeal(selectedMenuItems);

    }
}
