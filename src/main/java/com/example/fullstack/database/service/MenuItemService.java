package com.example.fullstack.database.service;

import com.example.fullstack.database.model.MenuItem;
import com.example.fullstack.database.model.MenuItemCategory;
import com.example.fullstack.database.model.Size;

import java.util.List;
import java.util.Optional;

public interface MenuItemService {
    void addMenuItem(MenuItem menuItem);
    Optional<MenuItem> getMenuItemById(String id);
    List<MenuItem> getAllMenuItems();
    List<MenuItem> getMenuItemsByCategoryAndSize(MenuItemCategory category, Size size);
    List<MenuItem> getMenuItemByAvailability(boolean isAvailable);
    Optional<MenuItem> getMenuItemByName(String name);
    void addAllMenuItems(List<MenuItem> menuItems);
    List<MenuItem> getMenuItemsByCategory(MenuItemCategory category);
    void deleteAllMenuItems();




}
