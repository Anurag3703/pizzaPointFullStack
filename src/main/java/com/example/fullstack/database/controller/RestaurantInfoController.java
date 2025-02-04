package com.example.fullstack.database.controller;

import com.example.fullstack.database.model.RestaurantInfo;
import com.example.fullstack.database.service.implementation.RestaurantInfoServiceImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurant")
public class RestaurantInfoController {

    RestaurantInfoServiceImpl restaurantInfoServiceImpl;
    public RestaurantInfoController(RestaurantInfoServiceImpl restaurantInfoServiceImpl) {
        this.restaurantInfoServiceImpl = restaurantInfoServiceImpl;
    }

    @PostMapping
    public String addRestaurantInfo(@RequestBody RestaurantInfo restaurantInfo) {
        restaurantInfoServiceImpl.addRestaurantInfo(restaurantInfo);
        return "Restaurant Info Added Successfully";
    }
}
