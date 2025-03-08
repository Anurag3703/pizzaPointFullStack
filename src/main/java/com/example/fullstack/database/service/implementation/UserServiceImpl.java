package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.model.User;
import com.example.fullstack.database.repository.UserRepository;
import com.example.fullstack.database.service.UserService;
import com.example.fullstack.security.model.UserSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class UserServiceImpl implements UserService {

    private final  UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public void createUser(User user) {


        userRepository.save(user);

    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> addAllUsers(List<User> users) {
        return userRepository.saveAll(users);
    }

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserSecurity) {
            UserSecurity userSecurity = (UserSecurity) principal;
            return getUserFromUserSecurity(userSecurity);
        }
        return null; // User is not authenticated
    }
    private User getUserFromUserSecurity(UserSecurity userSecurity) {
        // Convert UserSecurity to User
        return userRepository.findByEmail(userSecurity.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    }



