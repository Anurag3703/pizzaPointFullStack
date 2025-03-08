package com.example.fullstack.security.controller;


import com.example.fullstack.database.model.User;
import com.example.fullstack.database.repository.UserRepository;
import com.example.fullstack.database.service.implementation.CartServiceImpl;
import com.example.fullstack.security.model.AuthRequest;
import com.example.fullstack.security.model.AuthResponse;
import com.example.fullstack.security.model.SignupRequest;
import com.example.fullstack.security.model.UserSecurity;
import com.example.fullstack.security.repository.SecurityUserRepository;
import com.example.fullstack.security.util.JwtTokenUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;
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
    private final CartServiceImpl cartServiceImpl;


    public EntryController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, SecurityUserRepository securityUserRepository, PasswordEncoder passwordEncoder, UserRepository userRepository, CartServiceImpl cartServiceImpl) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.securityUserRepository = securityUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository= userRepository;
        this.cartServiceImpl = cartServiceImpl;

    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody AuthRequest authRequest, HttpSession session) {
       try {
           Authentication authentication = new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword());
           Authentication authenticated = authenticationManager.authenticate(authentication);
           UserSecurity userSecurity = (UserSecurity) authenticated.getPrincipal();
           User user = userSecurity.getUser();
           cartServiceImpl.transferGuestCartToUser(session, user);
           String token = jwtTokenUtil.generateToken(userSecurity);
           return ResponseEntity.ok(new AuthResponse(token, user.getEmail()));
       }
       catch (BadCredentialsException e) {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
       }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest,HttpSession session) {
        try {



            if (securityUserRepository.existsByEmail(signupRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already taken");
            }
            if (!signupRequest.isPasswordValid()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password must be at least 10 characters long, contain at least one uppercase letter, one special character, and match the confirmation password.");
            }


            // Create and save the UserSecurity
            UserSecurity userSecurity = new UserSecurity();
            userSecurity.setEmail(signupRequest.getEmail());
            userSecurity.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
            userSecurity.setRole("USER");
            UserSecurity savedUserSecurity = securityUserRepository.save(userSecurity);


            User user = new User();
            user.setEmail(signupRequest.getEmail());
            user.setName(signupRequest.getName());
            user.setAddress(signupRequest.getAddress());
            user.setPhone(signupRequest.getPhone());

            user.setUserSecurity(savedUserSecurity);


            savedUserSecurity.setUser(user);


            userRepository.save(user);

            // Transfer guest cart to user cart
            cartServiceImpl.transferGuestCartToUser(session, user);

            // Authenticate the user to generate the JWT token
            Authentication authentication = new UsernamePasswordAuthenticationToken(userSecurity.getEmail(), signupRequest.getPassword());
            Authentication authenticated = authenticationManager.authenticate(authentication);
            // Token
            String token = jwtTokenUtil.generateToken((UserSecurity) authenticated.getPrincipal());
            return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token, userSecurity.getEmail()));

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during signup. Please try again later.");
        }
    }

    @GetMapping("/test-auth")
    public ResponseEntity<?> testAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok("Authenticated User: " + authentication.getName());
    }
}
