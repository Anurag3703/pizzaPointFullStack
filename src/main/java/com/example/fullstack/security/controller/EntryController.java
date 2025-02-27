package com.example.fullstack.security.controller;


import com.example.fullstack.database.model.User;
import com.example.fullstack.database.repository.UserRepository;
import com.example.fullstack.security.model.AuthRequest;
import com.example.fullstack.security.model.AuthResponse;
import com.example.fullstack.security.model.SignupRequest;
import com.example.fullstack.security.model.UserSecurity;
import com.example.fullstack.security.repository.SecurityUserRepository;
import com.example.fullstack.security.util.JwtTokenUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class EntryController {


    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final SecurityUserRepository securityUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;


    public EntryController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, SecurityUserRepository securityUserRepository, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.securityUserRepository = securityUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository= userRepository;

    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody AuthRequest authRequest) {
       try {
           Authentication authentication = new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword());
           Authentication authenticated = authenticationManager.authenticate(authentication);
           UserSecurity user = (UserSecurity) authenticated.getPrincipal();
           String token = jwtTokenUtil.generateToken(user);
           return ResponseEntity.ok(new AuthResponse(token, user.getEmail()));
       }
       catch (BadCredentialsException e) {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
       }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
        try {



            if (securityUserRepository.existsByEmail(signupRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already taken");
            }
            if (!signupRequest.isPasswordValid()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password must be at least 10 characters long, contain at least one uppercase letter, one special character, and match the confirmation password.");
            }


            UserSecurity user = new UserSecurity();
            user.setEmail(signupRequest.getEmail());
            user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
            user.setRole("USER");
            UserSecurity savedUser = securityUserRepository.save(user);

            User users = new User();
            users.setEmail(signupRequest.getEmail());
            users.setAddress(signupRequest.getAddress());
            users.setName(signupRequest.getName());
            users.setPhone(signupRequest.getPhone());

            users.setUserSecurity(savedUser);

            // Save the User object in the database
            userRepository.save(users);


            // Authenticate the user to generate the JWT token
            Authentication authentication = new UsernamePasswordAuthenticationToken(savedUser.getEmail(), signupRequest.getPassword());
            Authentication authenticated = authenticationManager.authenticate(authentication);
            // Token
            String token = jwtTokenUtil.generateToken((UserSecurity) authenticated.getPrincipal());
            return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token, savedUser.getEmail()));

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during signup. Please try again later.");
        }
    }
}
