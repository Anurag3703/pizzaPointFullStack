package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.Orders;
import com.example.fullstack.database.model.Status;
import com.example.fullstack.database.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Stack;


public interface OrdersRepository extends JpaRepository<Orders, String> {
    List<Orders> findByUserId(Long userId);
    List<Orders> findByUserEmail(String email);
    Optional<Orders> findTopByUserAndStatusOrderByCreatedAtDesc(User user, Status status);
    List<Orders> findByStatusNotOrderByCreatedAtDesc(Status status);
    List<Orders> findByUserEmailAndStatusNot(String email, Status status);
    void deleteByStatus(Status status);
    List<Orders> findByStatus(Status status);

}
