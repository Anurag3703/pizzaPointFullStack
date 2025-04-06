package com.example.fullstack.database.dto.service;

import com.example.fullstack.database.dto.AddressDTO;
import com.example.fullstack.database.model.Address;

public interface AddressDTOService {
    AddressDTO convertDTO(Address address);
}
