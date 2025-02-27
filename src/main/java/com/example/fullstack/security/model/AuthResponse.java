package com.example.fullstack.security.model;


import lombok.AllArgsConstructor;
import lombok.Data;


public class AuthResponse {
    private String token;
    private String email;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEm() {
        return email;
    }

    public void setEmail(String username) {
        this.email = username;
    }

    public AuthResponse(String token, String username) {
        this.token = token;
        this.email = username;
    }

    public AuthResponse() {
    }
}
