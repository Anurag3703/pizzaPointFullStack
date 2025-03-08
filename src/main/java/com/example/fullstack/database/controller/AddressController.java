package com.example.fullstack.database.controller;

import com.example.fullstack.database.model.Address;
import com.example.fullstack.database.service.implementation.AddressServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/address")
public class AddressController {
    private final AddressServiceImpl addressServiceImpl;
    public AddressController(AddressServiceImpl addressServiceImpl) {
        this.addressServiceImpl = addressServiceImpl;
    }

    @GetMapping("/all")
    public List<Address> getAllAddress() {
        return addressServiceImpl.getAllAddresses();
    }

    @GetMapping("/selected")
    public ResponseEntity<Address> getSelectedAddress(@RequestParam boolean isSelected) {
        Optional<Address> address = addressServiceImpl.getAddressByIsSelected(isSelected);
        return address.map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Selected address not found"));
    }

    @PostMapping("/add")
    public ResponseEntity<String> addAddress(@RequestBody Address address) {
        try {
            addressServiceImpl.saveAddress(address);
            return ResponseEntity.ok("Address saved successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
