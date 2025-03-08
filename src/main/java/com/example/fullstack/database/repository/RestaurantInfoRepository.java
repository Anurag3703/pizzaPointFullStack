package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.RestaurantInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantInfoRepository extends JpaRepository<RestaurantInfo, Integer> {

}
