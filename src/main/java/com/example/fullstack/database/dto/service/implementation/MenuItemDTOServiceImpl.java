package com.example.fullstack.database.dto.service.implementation;

import com.example.fullstack.database.dto.MenuItemDTO;
import com.example.fullstack.database.dto.service.MenuItemDTOService;
import com.example.fullstack.database.model.MenuItem;
import org.springframework.stereotype.Service;

@Service
public class MenuItemDTOServiceImpl implements MenuItemDTOService {
    @Override
    public MenuItemDTO convertToDTO(MenuItem menuItem) {
        MenuItemDTO menuItemDTO = new MenuItemDTO();
        menuItemDTO.setId(menuItem.getId());
        menuItemDTO.setName(menuItem.getName());
        menuItemDTO.setDescription(menuItem.getDescription());
        menuItemDTO.setPrice(menuItem.getPrice());
        menuItemDTO.setImageUrl(menuItem.getImageUrl());
        menuItemDTO.setCategory(menuItem.getCategory());
        menuItemDTO.setSize(menuItem.getSize());
        menuItemDTO.setIsAvailable(menuItem.getIsAvailable());
        return menuItemDTO;

    }
}
