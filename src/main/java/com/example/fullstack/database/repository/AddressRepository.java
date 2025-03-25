package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.Address;
import com.example.fullstack.database.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository  extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
    Optional<Address> findByUserIdAndSelected(Long userId, boolean isSelected);
    Optional<Address> findByBuildingNameAndStreetAndApartmentNoAndUser(
            String buildingName, String street, String apartmentNo, User user);
    List<Address> findByUser(User user);
    Address findByUserAndSelected(User user, boolean isSelected);
    Address findTopByUserOrderByCreatedDateTimeAsc(User user);
    Long countByUser(User user);
    @Modifying
    @Query("UPDATE Address a SET a.selected = false WHERE a.user.id = :userId")
    void unselectAllAddressesForUser(@Param("userId") Long userId);
}
