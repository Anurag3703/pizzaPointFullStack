package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.model.MenuItem;
import com.example.fullstack.database.model.MenuItemCategory;
import com.example.fullstack.database.model.Size;
import com.example.fullstack.database.repository.MenuItemRepository;
import com.example.fullstack.database.service.MenuItemService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MenuItemServiceImpl implements MenuItemService {


    MenuItemRepository menuItemRepository;

    public MenuItemServiceImpl(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    public void addMenuItem(MenuItem menuItem) {
        menuItemRepository.save(menuItem);

    }

    @Override
    public Optional<MenuItem> getMenuItemById(String id) {
        return menuItemRepository.findById(id);
    }

    @Override
    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    @Override
    public List<MenuItem> getMenuItemsByCategoryAndSize(MenuItemCategory category, Size size) {
        return menuItemRepository.findAllByCategoryAndSize(category, size);
    }



    @Override
    public List<MenuItem> getMenuItemByAvailability(boolean isAvailable) {
        return menuItemRepository.findByIsAvailable(isAvailable);
    }

    @Override
    public Optional<MenuItem> getMenuItemByName(String name) {
        return menuItemRepository.findByName(name);
    }

    @Override
    public void addAllMenuItems(List<MenuItem> menuItems) {
        menuItemRepository.saveAll(menuItems);
    }

    @Override
    public List<MenuItem> getMenuItemsByCategory(MenuItemCategory category) {
        return menuItemRepository.findByCategory(category);
    }

    @Override
    public void deleteAllMenuItems() {
        menuItemRepository.deleteAll();
    }

    @Override
    public List<MenuItem> getMenuItemByNameAndSize(String name, Size size) {
        return menuItemRepository.findByNameAndSize(name, size);
    }

    @Override
    @Transactional
    public void updateMenuItem(String menuItemId,MenuItem menuItem) {
        MenuItem updatedMenuItem = menuItemRepository.findById(menuItemId).orElseThrow(() -> new EntityNotFoundException("Menu Item Not with id: " + menuItemId));

        if(menuItem.getName() != null){
            updatedMenuItem.setName(menuItem.getName());
        }
        if(menuItem.getSize() != null){
            updatedMenuItem.setSize(menuItem.getSize());
        }
        if(menuItem.getCategory() != null){
            updatedMenuItem.setCategory(menuItem.getCategory());
        }
        if(menuItem.getIsAvailable() != null){
            updatedMenuItem.setIsAvailable(menuItem.getIsAvailable());
        }
        if(menuItem.getPrice() != null){
            updatedMenuItem.setPrice(menuItem.getPrice());
        }
        if(menuItem.getDescription() != null){
            updatedMenuItem.setDescription(menuItem.getDescription());
        }
        if(menuItem.getImageUrl() != null){
            updatedMenuItem.setImageUrl(menuItem.getImageUrl());
        }
    }

}
