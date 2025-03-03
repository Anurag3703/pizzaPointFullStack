package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.MenuItem;
import com.example.fullstack.database.model.MenuItemCategory;
import com.example.fullstack.database.model.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, String> {
    List<MenuItem> findAllByCategoryAndSize(MenuItemCategory category, Size size);
    List<MenuItem> findAllBySize(Size size);
    Optional<MenuItem> findByName(String name);
    void deleteByName(MenuItemCategory name);
    void deleteByCategory(MenuItemCategory category);
    void deleteBySize(Size size);
    void deleteAll();
    List<MenuItem> findByIsAvailable(Boolean isAvailable);
    List<MenuItem> findByCategory(MenuItemCategory category);
    List<MenuItem> findByCategoryAndIsAvailable(MenuItemCategory category, Boolean isAvailable);
    List<MenuItem> findByNameAndSize(String name, Size size);


}
