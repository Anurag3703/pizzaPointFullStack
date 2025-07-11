package com.example.fullstack.database.repository;

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

    List<Orders> findByStatusOrderByCreatedAtDesc(Status status);

    List<Orders> findByStatusInOrderByCreatedAtDesc(List<Status> statuses);


    List<Orders> findByUserAndStatusNotIn(User user, List<Status> list);
}
