package com.example.fullstack.database.controller;

import com.example.fullstack.database.model.RestaurantInfo;
import com.example.fullstack.database.service.implementation.RestaurantInfoServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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

    @PreAuthorize("hasRole('ADMIN')")
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/restaurant-status")
    public ResponseEntity<?> getRestaurantStatus() {
        try{
            boolean isOpen = restaurantInfoServiceImpl.isRestaurantOpen();
            Map<String, Boolean> restaurantStatus = new HashMap<>();
            restaurantStatus.put("isOpen", isOpen);
            return ResponseEntity.ok(restaurantStatus);

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
