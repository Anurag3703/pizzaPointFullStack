package com.example.fullstack.security.controller;


import com.example.fullstack.database.model.User;
import com.example.fullstack.database.repository.DeletedUserRepository;
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

import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/auth")
public class EntryController {


    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final SecurityUserRepository securityUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final CartServiceImpl cartServiceImpl;


    public EntryController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, SecurityUserRepository securityUserRepository, PasswordEncoder passwordEncoder, UserRepository userRepository, CartServiceImpl cartServiceImpl, DeletedUserRepository deletedUserRepository) {
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
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
        try {


            if (securityUserRepository.existsByEmail(signupRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already taken");
            }
            if (!signupRequest.isPasswordValid()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password must be at least 10 characters long, contain at least one uppercase letter, one special character, and match the confirmation password.");
            }

//            if(!deletedUserRepository.findByPhone(signupRequest.getPhone()).isEmpty()) {
//                return ResponseEntity.status(HttpStatus.CONFLICT).body("Phone Number is blocked");
//            }
            // Create and save the UserSecurity
            UserSecurity userSecurity = new UserSecurity();
            userSecurity.setEmail(signupRequest.getEmail());
            userSecurity.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
            userSecurity.setPhone(signupRequest.getPhone());
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
//            cartServiceImpl.transferGuestCartToUser(session, user);

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

    @GetMapping("/valid-token")
    public ResponseEntity<?> validateToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()) {
            return  ResponseEntity.ok("Authenticated User: " + authentication.getName());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
    }

    @GetMapping("/validate-token/{email}")
    public ResponseEntity<?> validateTokenForUser(@PathVariable String email) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserSecurity userSecurity) {
                // First, try to match directly on the UserSecurity email
                if (userSecurity.getEmail() != null && userSecurity.getEmail().equals(email)) {
                    return ResponseEntity.ok("Token is valid for user with email : " + authentication.getName());
                }
                // If I have a User object inside UserSecurity, checking that too
                if (userSecurity.getUser() != null && userSecurity.getUser().getEmail() != null
                        && userSecurity.getUser().getEmail().equals(email)) {
                    return ResponseEntity.ok("Authenticated User: " + authentication.getName());
                }
                // If neither matches, return forbidden
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Token does not belong to this user");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Unexpected principal type: " + (principal != null ? principal.getClass().getName() : "null"));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
    }


    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean exists = securityUserRepository.existsByEmail(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/check-email-password")
    public ResponseEntity<?> checkEmailPassword(@RequestParam String email, @RequestParam String password) {
        Optional<UserSecurity> optionalUser = securityUserRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
        UserSecurity userSecurity = optionalUser.get();
        if (!passwordEncoder.matches(password, userSecurity.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
        return ResponseEntity.ok("Email and password are valid");
    }

}
