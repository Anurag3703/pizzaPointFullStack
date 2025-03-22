package com.example.fullstack.database.dto;


import lombok.Data;

@Data
public class AddressDTO {
    private Long addressId;
    private String buildingName;
    private String floor;
    private String intercom;
    private String apartmentNo;
    private String street;
    private String otherInstructions;
    private boolean selected;
    private boolean recent;
    private Long userId;
}
