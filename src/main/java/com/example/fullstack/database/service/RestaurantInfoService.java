package com.example.fullstack.database.service;

import com.example.fullstack.database.model.RestaurantInfo;

public interface RestaurantInfoService {
    void addRestaurantInfo(RestaurantInfo restaurantInfo);
    void updateRestaurantOpenStatus(int restaurantId, boolean isOpen);
    void updateRestaurantInfo(RestaurantInfo restaurantInfo);
}
