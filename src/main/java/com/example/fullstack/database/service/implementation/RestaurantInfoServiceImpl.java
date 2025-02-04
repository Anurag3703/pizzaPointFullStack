package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.model.RestaurantInfo;
import com.example.fullstack.database.repository.RestaurantInfoRepository;
import com.example.fullstack.database.service.RestaurantInfoService;
import org.springframework.stereotype.Service;

@Service
public class RestaurantInfoServiceImpl implements RestaurantInfoService {

    RestaurantInfoRepository restaurantInfoRepository;
    public RestaurantInfoServiceImpl(RestaurantInfoRepository restaurantInfoRepository) {
        this.restaurantInfoRepository = restaurantInfoRepository;
    }
    @Override
    public void addRestaurantInfo(RestaurantInfo restaurantInfo) {
        restaurantInfoRepository.save(restaurantInfo);

    }
}
