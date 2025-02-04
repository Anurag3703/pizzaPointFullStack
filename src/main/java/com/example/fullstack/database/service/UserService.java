package com.example.fullstack.database.service;

import com.example.fullstack.database.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    void createUser(User user);
    Optional<User> getUserById(long id);
    List<User> addAllUsers(List<User> users);

}
