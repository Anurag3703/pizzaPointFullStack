package com.example.fullstack.database.dto.service.implementation;

import com.example.fullstack.database.dto.AddressDTO;
import com.example.fullstack.database.dto.service.AddressDTOService;
import com.example.fullstack.database.model.Address;
import org.springframework.stereotype.Service;

@Service
public class AddressDTOServiceImpl implements AddressDTOService {
    @Override
    public AddressDTO convertDTO(Address address) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressId(address.getAddressId());
        addressDTO.setOtherInstructions(address.getOtherInstructions());
        addressDTO.setStreet(address.getStreet());
        addressDTO.setApartmentNo(address.getApartmentNo());
        addressDTO.setFloor(address.getFloor());  // Fixing the setter for floor
        addressDTO.setSelected(address.isSelected());
        addressDTO.setIntercom(address.getIntercom());
        addressDTO.setRecent(address.isRecent());
        addressDTO.setBuildingName(address.getBuildingName());
        addressDTO.setUserId(address.getUser().getId());

        // Return the mapped DTO
        return addressDTO;

    }
}
