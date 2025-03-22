package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrdersRepository extends JpaRepository<Orders, String> {

}
