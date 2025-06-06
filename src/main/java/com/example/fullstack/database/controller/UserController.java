package com.example.fullstack.database.controller;

import com.example.fullstack.database.model.User;
import com.example.fullstack.database.service.implementation.UserServiceImpl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    UserServiceImpl userServiceImpl;
    User user;
    public UserController(UserServiceImpl userServiceImpl) {
        this.userServiceImpl = userServiceImpl;
    }

    @PostMapping
    public String addUser(@RequestBody User user) {
        System.out.println("Received User: " + user);
        userServiceImpl.createUser(user);
        return "Added User: " + user;
    }

    @GetMapping("/get/{id}")
    public User getUser(@PathVariable Long id) {
        return userServiceImpl.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }

    @PostMapping("/all")
    public List<User> getAllUsers(@RequestBody List<User> users) {
        return userServiceImpl.addAllUsers(users);
    }

    @PostMapping("/delete/{email}")
    public ResponseEntity<?> deleteUser(@PathVariable String email) {
        try{
            userServiceImpl.deleteUser(email);
            return ResponseEntity.ok("Deleted User: " + email);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }





}
