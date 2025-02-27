package com.example.fullstack.security.model;


import java.util.regex.Pattern;

public class SignupRequest {
    private String name;
    private String email;
    private String password;
    private String confirmPassword;
    private String phone;
    private String address;

    public SignupRequest(String firstName, String email, String password, String confirmPassword, String phone, String address) {
        this.name = firstName;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.phone = phone;
        this.address = address;
    }

    public SignupRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String firstName) {
        this.name = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phoneNumber) {
        this.phone = phoneNumber;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

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



