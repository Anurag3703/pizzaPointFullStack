package com.example.fullstack.database.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressValidationRequestDTO {
    private String street;
    private double latitude;
    private double longitude;
}
