package com.example.fullstack.database.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private UserSecurityDTO userSecurity;
    private List<Long> orderIds;
    private List<Long> reviewIds;
    private List<Long> addressIds;
}