package com.example.fullstack.database.service;

import com.example.fullstack.database.model.Address;

import java.util.List;
import java.util.Optional;

public interface AddressService {
    List<Address> getAllAddresses();
    Optional<Address> getAddressByIsSelected(boolean isSelected);
    void saveAddress(Address address);
}
