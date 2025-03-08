package com.example.fullstack.security.model;


import lombok.*;

import java.util.regex.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SignupRequest {
    private String name;
    private String email;
    private String password;
    private String confirmPassword;
    private String phone;
    private String address;

    public boolean isPasswordValid() {
        if (password == null || password.isEmpty()) {
            return false;
        }

        if (password.length() < 10) {
            return false;
        }

        // Regex
        String passwordPattern = "^(?=.*[A-Z])(?=.*[\\W_]).{10,}$";
        if (!Pattern.matches(passwordPattern, password)) {
            return false;
        }

        // Password must match confirmPassword
        if (!password.equals(confirmPassword)) {
            return false;
        }

        return true;
    }
}



