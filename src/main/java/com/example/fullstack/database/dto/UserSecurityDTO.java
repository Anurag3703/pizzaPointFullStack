package com.example.fullstack.database.dto;

import com.example.fullstack.database.dto.UserDTO;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

@Data
public class UserSecurityDTO {
    private Long id;
    private String email;
    private String password;
    private String role;
    private String phone;
    private boolean isMobileVerified;
    private UserDTO user;
}