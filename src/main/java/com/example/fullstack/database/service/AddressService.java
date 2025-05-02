package com.example.fullstack.database.service;

import com.example.fullstack.database.model.Address;
import com.example.fullstack.database.model.User;

import java.util.List;
import java.util.Optional;

public interface AddressService {
    List<Address> getAllAddresses();
    Optional<Address> getAddressByIsSelected(boolean isSelected);
    void saveAddress(Address address);
    void addAddress(Address address);
    Address getAddressByUserAndIsSelected(User user, boolean isSelected);
    Address getAddressById(Long id);
    void setAddressAsSelected(Long addressId);
}
