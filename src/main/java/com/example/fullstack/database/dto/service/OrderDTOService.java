package com.example.fullstack.database.dto.service;

import com.example.fullstack.database.dto.OrderDTO;
import com.example.fullstack.database.model.Orders;

public interface OrderDTOService {
    OrderDTO convertToDTO(Orders order);
}
