package com.example.fullstack.database.controller;

import com.example.fullstack.database.dto.AddressDTO;
import com.example.fullstack.database.dto.service.implementation.AddressDTOServiceImpl;
import com.example.fullstack.database.model.Address;
import com.example.fullstack.database.service.implementation.AddressServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/address")
public class AddressController {
    private final AddressServiceImpl addressServiceImpl;
    private final ModelMapper modelMapper;
    private final AddressDTOServiceImpl addressDTOServiceImpl;
    public AddressController(AddressServiceImpl addressServiceImpl, ModelMapper modelMapper, AddressDTOServiceImpl addressDTOServiceImpl) {
        this.addressServiceImpl = addressServiceImpl;
        this.modelMapper = modelMapper;
        this.addressDTOServiceImpl = addressDTOServiceImpl;
    }

    @GetMapping("/all")
    public List<AddressDTO> getAllAddress() {
        // Fetch all Address entities
        List<Address> addresses = addressServiceImpl.getAllAddresses();


        return addresses.stream()
                .map(addressDTOServiceImpl::convertDTO)
                .collect(Collectors.toList());  // Return the list of AddressDTOs
    }



    @GetMapping("/selected")
    public ResponseEntity<AddressDTO> getSelectedAddress(@RequestParam boolean isSelected) {
        Optional<Address> address = addressServiceImpl.getAddressByIsSelected(isSelected);
        return address.map(addr -> {
            AddressDTO addressDTO = addressDTOServiceImpl.convertDTO(addr);
            return ResponseEntity.ok(addressDTO);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Selected address not found")); }

    @PostMapping(value = "/add",consumes = "application/json")
    public ResponseEntity<String> addAddress(@RequestBody Address address) {
        try {
            addressServiceImpl.saveAddress(address);
            return ResponseEntity.ok("Address saved successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/address/{id}")
    public ResponseEntity<?> getAddressById(@PathVariable long id) {
        try {
            Address address = addressServiceImpl.getAddressById(id);
            return ResponseEntity.ok(modelMapper.map(address, AddressDTO.class));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

    }

    @PostMapping("/address-selected/{id}")
    public ResponseEntity<String> addAddressSelected(@PathVariable Long id) {
        try {
            addressServiceImpl.setAddressAsSelected(id);
            return ResponseEntity.ok("Address set as selected successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
