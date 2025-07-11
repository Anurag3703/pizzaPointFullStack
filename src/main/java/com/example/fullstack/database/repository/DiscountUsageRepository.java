package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.Discount;
import com.example.fullstack.database.model.DiscountUsage;
import com.example.fullstack.database.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscountUsageRepository extends JpaRepository<DiscountUsage, Long> {
    boolean existsByDiscountAndUserPhone(Discount discount, String userPhone);
    List<DiscountUsage> findByDiscount(Discount discount);

    boolean existsByDiscountAndUser(Discount discount, User user);
}
