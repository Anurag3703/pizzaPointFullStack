package com.example.fullstack.database.dto.service;

import com.example.fullstack.database.dto.MenuItemDTO;
import com.example.fullstack.database.model.MenuItem;

public interface MenuItemDTOService {
    MenuItemDTO convertToDTO(MenuItem menuItem);
}
