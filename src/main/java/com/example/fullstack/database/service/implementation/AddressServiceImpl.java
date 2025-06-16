package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.dto.AddressValidationRequestDTO;
import com.example.fullstack.database.dto.AddressValidationResponseDTO;
import com.example.fullstack.database.model.Address;
import com.example.fullstack.database.model.User;
import com.example.fullstack.database.repository.AddressRepository;
import com.example.fullstack.database.repository.UserRepository;
import com.example.fullstack.database.service.AddressService;
import com.example.fullstack.database.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service

public class AddressServiceImpl implements AddressService {


    private final AddressRepository addressRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    @Value("${google.api.key}")
    private String googleApiKey;
    private static final double STORE_LAT = 47.529989;
    private static final double STORE_LNG = 21.623746;
    public AddressServiceImpl(AddressRepository addressRepository, UserService userService,UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }


    @Override
    public List<Address> getAllAddresses() {
        User currentUser = userService.getCurrentUser();
        return addressRepository.findByUserId(currentUser.getId());
    }

    @Override
    public Optional<Address> getAddressByIsSelected(boolean isSelected) {
        User currentUser = userService.getCurrentUser();
        return Optional.ofNullable(addressRepository.findByUserIdAndSelected(currentUser.getId(), isSelected)
                .orElseThrow(() -> new RuntimeException("Selected address not found")));
    }

    @Override
    @Transactional
    public void saveAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }

        User currentUser = userService.getCurrentUser();
        address.setUser(currentUser);

        // Ensure the user exists
        if (!userRepository.existsById(currentUser.getId())) {
            throw new IllegalArgumentException("User not found");
        }

        // Check if this address already exists for the user
        Optional<Address> existingAddress = addressRepository.findByBuildingNameAndStreetAndApartmentNoAndUser(
                address.getBuildingName(),
                address.getStreet(),
                address.getApartmentNo(),
                currentUser
        );

        if (existingAddress.isPresent()) {
            throw new IllegalArgumentException("Address already exists for the current user.");
        }

        // If user selects new address
        if (address.isSelected()) {
            List<Address> userAddresses = addressRepository.findByUser(currentUser);
            for (Address addr : userAddresses) {
                if (addr.isSelected()) {
                    addr.setSelected(false);  // Unset previous default address
                }
            }
            // Save the updated addresses
            addressRepository.saveAll(userAddresses);
        }

        long addressCount = addressRepository.countByUser(currentUser);

        // If there are already 3 addresses, delete the oldest one
        if (addressCount >= 100) {
            Address oldestAddress = addressRepository.findTopByUserOrderByCreatedDateTimeAsc(currentUser);
            if (oldestAddress != null) {
                addressRepository.delete(oldestAddress);
            }
        }

        // Save the new address
        addressRepository.save(address);
    }


    @Override
    public void addAddress(Address address) {


    }

    @Override
    public Address getAddressByUserAndIsSelected(User user, boolean isSelected) {
        return addressRepository.findByUserAndSelected(user,isSelected);
    }

    @Override
    public Address getAddressById(Long id) {
        return addressRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void setAddressAsSelected(Long addressId) {
        User currentUser = userService.getCurrentUser();
        Address addressToSelect = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!addressToSelect.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Address does not belong to current user");
        }

        // Unselect all addresses for this user
        List<Address> userAddresses = addressRepository.findByUser(currentUser);
        for (Address addr : userAddresses) {
            if (addr.isSelected()) {
                addr.setSelected(false);
            }
        }
        addressRepository.saveAll(userAddresses);

        // set
        addressToSelect.setSelected(true);
        addressRepository.save(addressToSelect);

    }

    @Override
    public AddressValidationResponseDTO validateAddress(AddressValidationRequestDTO request) {
        try {
            String origin = STORE_LAT + "," + STORE_LNG;
            String destination = request.getLatitude() + "," + request.getLongitude();

            String url = "https://maps.googleapis.com/maps/api/distancematrix/json" +
                    "?origins=" + origin +
                    "&destinations=" + destination +
                    "&key=" + googleApiKey;

            RestTemplate restTemplate = new RestTemplate();
            Map response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("rows")) {
                List rows = (List) response.get("rows");
                if (!rows.isEmpty()) {
                    Map row = (Map) rows.get(0);
                    List elements = (List) row.get("elements");
                    if (!elements.isEmpty()) {
                        Map element = (Map) elements.get(0);
                        Map distance = (Map) element.get("distance");
                        if (distance != null && distance.containsKey("value")) {
                            int meters = (int) distance.get("value");
                            return new AddressValidationResponseDTO(meters <= 5000);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new AddressValidationResponseDTO(false);

    }
}
