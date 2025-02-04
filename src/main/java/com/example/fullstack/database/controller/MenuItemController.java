package com.example.fullstack.database.controller;

import com.example.fullstack.database.model.MenuItem;
import com.example.fullstack.database.model.MenuItemCategory;
import com.example.fullstack.database.model.Size;
import com.example.fullstack.database.service.implementation.MenuItemServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menuItem")
public class MenuItemController {

    MenuItemServiceImpl menuItemServiceImpl;
    public MenuItemController(MenuItemServiceImpl menuItemServiceImpl) {
        this.menuItemServiceImpl = menuItemServiceImpl;
    }

    @PostMapping
    public String addMenuItems(@RequestBody MenuItem menuItem) {
        menuItemServiceImpl.addMenuItem(menuItem);
        return "Success";
    }
    @GetMapping("/get/{id}")
    public MenuItem getMenuItemById(@PathVariable String id) {
        return menuItemServiceImpl.getMenuItemById(id) .orElseThrow(() -> new RuntimeException("MenuItem not found with ID: " + id));
    }



    @PostMapping("/all")
    public String addMenuItems(@RequestBody List<MenuItem> menuItems) {
        menuItemServiceImpl.addAllMenuItems(menuItems);
        return "Success";
    }

    @GetMapping("/items")
    public List<MenuItem> getMenuItemsByCategoryAndSize(
            @RequestParam MenuItemCategory category,
            @RequestParam Size size) {
        return menuItemServiceImpl.getMenuItemsByCategoryAndSize(category, size);
    }

    @GetMapping("/get/category/{category}")
    public List<MenuItem> getMenuItemsByCategory(@PathVariable MenuItemCategory category) {
        return menuItemServiceImpl.getMenuItemsByCategory(category);

    }

    @DeleteMapping("/delete/all")
    public String deleteAllMenuItems() {
        menuItemServiceImpl.deleteAllMenuItems();
        return "Deleted all menu items";
    }
}
