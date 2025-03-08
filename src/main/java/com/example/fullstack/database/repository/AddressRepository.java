package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.Address;
import com.example.fullstack.database.model.User;
import org.hibernate.sql.ast.tree.expression.JdbcParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository  extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
    Optional<Address> findByUserIdAndIsSelected(Long userId, boolean isSelected);
    Optional<Address> findByBuildingNameAndStreetAndApartmentNoAndUser(
            String buildingName, String street, String apartmentNo, User user);
}
