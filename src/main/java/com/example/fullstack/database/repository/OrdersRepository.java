package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.OrderItem;
import com.example.fullstack.database.model.Orders;
import com.example.fullstack.database.model.Status;
import com.example.fullstack.database.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Stack;


public interface OrdersRepository extends JpaRepository<Orders, String> {
    List<Orders> findByUserId(Long userId);
    List<Orders> findByUserEmail(String email);
    Optional<Orders> findTopByUserAndStatusOrderByCreatedAtDesc(User user, Status status);
    List<Orders> findByStatusNotInOrderByCreatedAtDesc(List<Status> statuses);

    List<Orders> findByUserEmailAndStatusNotOrderByCreatedAtDesc(String email, Status status);
    void deleteByStatus(Status status);
    List<Orders> findByStatus(Status status);
    @Query(value = """
    SELECT * FROM orders
    ORDER BY
      CASE 
        WHEN status IN ('FAILED', 'CANCELLED', 'DELIVERED') THEN 1
        ELSE 0
      END,
      created_at DESC
""", nativeQuery = true)
    List<Orders> findOrdersPrioritizingActiveStatuses();
    @Query("SELECT o FROM Orders o WHERE o.orderId = :orderId AND o.user.id = :userId")
    Optional<Orders> findByOrderIdAndUserId(@Param("orderId") String orderId, @Param("userId") Long userId);


    @Query("SELECT DISTINCT o FROM Orders o " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.address " +
            "WHERE o.user.email = :email AND o.status != :status " +
            "ORDER BY o.createdAt DESC")
    List<Orders> findByUserEmailAndStatusNotWithFetchOrderByCreatedAtDesc(
            @Param("email") String email,
            @Param("status") Status status
    );

    // Add this additional method to fetch order items separately:
    @Query("SELECT DISTINCT oi FROM OrderItem oi " +
            "LEFT JOIN FETCH oi.menuItem " +
            "LEFT JOIN FETCH oi.customMeal " +
            "WHERE oi.order IN :orders")
    List<OrderItem> findOrderItemsWithDetailsByOrders(@Param("orders") List<Orders> orders);
}
