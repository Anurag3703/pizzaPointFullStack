package com.example.fullstack.database.dto.service;

import com.example.fullstack.database.dto.CartDTO;
import com.example.fullstack.database.model.Cart;

public interface CartDTOService {
        CartDTO convertToDTO(Cart cart);
}
