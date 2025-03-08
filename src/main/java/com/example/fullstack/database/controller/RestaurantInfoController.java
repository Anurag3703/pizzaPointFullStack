package com.example.fullstack.database.controller;

import com.example.fullstack.database.model.RestaurantInfo;
import com.example.fullstack.database.service.implementation.RestaurantInfoServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/{restaurantId}/status")
    public ResponseEntity<String> updateRestaurantStatus(
            @PathVariable int restaurantId,
            @RequestParam boolean isOpen) {
        try {
            restaurantInfoServiceImpl.updateRestaurantOpenStatus(restaurantId, isOpen);
            return ResponseEntity.ok("Restaurant status updated successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
