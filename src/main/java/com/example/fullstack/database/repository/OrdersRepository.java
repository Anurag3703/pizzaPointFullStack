package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
}
