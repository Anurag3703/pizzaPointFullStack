package com.example.fullstack.database.dto;

import lombok.Data;

@Data
public class UnhashedUserDTO {
    private Long id;
    private String email;
    private String password;
    private String phone;
}