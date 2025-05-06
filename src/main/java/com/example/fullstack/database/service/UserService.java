package com.example.fullstack.database.service;

import com.example.fullstack.database.model.Cart;
import com.example.fullstack.database.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    void createUser(User user);
    Optional<User> getUserById(Long id);
    List<User> addAllUsers(List<User> users);
    User getCurrentUser();
    void deleteUser(String email);



}
