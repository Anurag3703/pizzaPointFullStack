package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.model.Address;
import com.example.fullstack.database.model.User;
import com.example.fullstack.database.repository.AddressRepository;
import com.example.fullstack.database.service.AddressService;
import com.example.fullstack.database.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AddressServiceImpl implements AddressService {


    private final AddressRepository addressRepository;
    private final UserService userService;
    public AddressServiceImpl(AddressRepository addressRepository, UserService userService) {
        this.addressRepository = addressRepository;
        this.userService = userService;
    }


    @Override
    public List<Address> getAllAddresses() {
        User currentUser = userService.getCurrentUser();
        return addressRepository.findByUserId(currentUser.getId());
    }

    @Override
    public Optional<Address> getAddressByIsSelected(boolean isSelected) {
        User currentUser = userService.getCurrentUser();
        return Optional.ofNullable(addressRepository.findByUserIdAndIsSelected(currentUser.getId(), isSelected)
                .orElseThrow(() -> new RuntimeException("Selected address not found")));
    }

    @Override
    @Transactional
    public void saveAddress(Address address) {
        User currentUser = userService.getCurrentUser();
        address.setUser(currentUser);
        Optional<Address> existingAddress = addressRepository.findByBuildingNameAndStreetAndApartmentNoAndUser(
                address.getBuildingName(),
                address.getStreet(),
                address.getApartmentNo(),
                currentUser
        );

        if (existingAddress.isPresent()) {
            throw new IllegalArgumentException("Address already exists for the current user.");
        }

        // Save the address if it doesn't exist
        addressRepository.save(address);


    }
}
