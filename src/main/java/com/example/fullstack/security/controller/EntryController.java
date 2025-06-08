package com.example.fullstack.security.controller;


import com.example.fullstack.database.model.User;
import com.example.fullstack.database.repository.DeletedUserRepository;
import com.example.fullstack.database.repository.UserRepository;
import com.example.fullstack.database.service.implementation.CartServiceImpl;
import com.example.fullstack.security.model.*;
import com.example.fullstack.security.repository.SecurityUserRepository;
import com.example.fullstack.security.service.OtpService;
import com.example.fullstack.security.service.ResetPasswordService;
import com.example.fullstack.security.service.SecurityEmailService;
import com.example.fullstack.security.util.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SignatureException;
import java.util.Date;
import java.util.HashMap;
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
    private final SecurityEmailService emailService;
    private final OtpService otpService;
    private final ResetPasswordService  resetPasswordService;


    public EntryController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, SecurityUserRepository securityUserRepository, PasswordEncoder passwordEncoder, UserRepository userRepository, CartServiceImpl cartServiceImpl, DeletedUserRepository deletedUserRepository
    , SecurityEmailService emailService, OtpService otpService, ResetPasswordService resetPasswordService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.securityUserRepository = securityUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository= userRepository;
        this.cartServiceImpl = cartServiceImpl;
        this.otpService = otpService;
        this.emailService = emailService;

        this.resetPasswordService = resetPasswordService;
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
    public ResponseEntity<?> validateTokenForUser(@PathVariable String email, HttpServletRequest request) {
        try {
            // First, extract and validate the token directly
            String token = extractTokenFromRequest(request);
            if (token == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "invalid");
                errorResponse.put("message", "No token provided");
                errorResponse.put("isExpired", false);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            // Check if token is expired before processing authentication
            if (jwtTokenUtil.isTokenExpired(token)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "expired");
                errorResponse.put("message", "Token is expired");
                errorResponse.put("isExpired", true);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            // Get authentication from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();

                if (principal instanceof UserSecurity userSecurity) {
                    // Check if the token belongs to the requested user
                    boolean emailMatches = false;

                    // First, try to match directly on the UserSecurity email
                    if (userSecurity.getEmail() != null && userSecurity.getEmail().equals(email)) {
                        emailMatches = true;
                    }
                    // If I have a User object inside UserSecurity, check that too
                    else if (userSecurity.getUser() != null && userSecurity.getUser().getEmail() != null
                            && userSecurity.getUser().getEmail().equals(email)) {
                        emailMatches = true;
                    }

                    if (emailMatches) {
                        // Get additional token info
                        Date expirationDate = jwtTokenUtil.getExpirationDateFromToken(token);
                        long timeUntilExpiry = expirationDate.getTime() - System.currentTimeMillis();

                        Map<String, Object> response = new HashMap<>();
                        response.put("status", "valid");
                        response.put("message", "Token is valid for user with email: " + email);
                        response.put("authenticatedUser", authentication.getName());
                        response.put("expiresAt", expirationDate);
                        response.put("timeUntilExpiry", timeUntilExpiry + " ms");
                        response.put("isExpired", false);

                        return ResponseEntity.ok(response);
                    } else {
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("status", "forbidden");
                        errorResponse.put("message", "Token does not belong to user with email: " + email);
                        errorResponse.put("isExpired", false);
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
                    }
                } else {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("status", "error");
                    errorResponse.put("message", "Unexpected principal type: " + (principal != null ? principal.getClass().getName() : "null"));
                    errorResponse.put("isExpired", false);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                }
            }

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "invalid");
            errorResponse.put("message", "Invalid Token");
            errorResponse.put("isExpired", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

        } catch (ExpiredJwtException e) {
            // Handle expired token exception
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "expired");
            errorResponse.put("message", "Token is expired");
            errorResponse.put("expiredAt", e.getClaims().getExpiration());
            errorResponse.put("isExpired", true);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

        } catch (MalformedJwtException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "malformed");
            errorResponse.put("message", "Malformed JWT token");
            errorResponse.put("isExpired", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Error validating token: " + e.getMessage());
            errorResponse.put("isExpired", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Helper method to extract token from request
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
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


    @PostMapping("/admin-login")
    public ResponseEntity<?> adminLogin(@RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword());
            Authentication authenticated = authenticationManager.authenticate(authentication);
            UserSecurity userSecurity = (UserSecurity) authenticated.getPrincipal();

            if(!"ADMIN".equals(userSecurity.getRole())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access Denied. Not an admin.");
            }

            String otp = otpService.generateOtp(userSecurity.getEmail());
            emailService.sendEmail(userSecurity.getEmail(), otp);
            boolean otpSent = true;

            return ResponseEntity.ok(Map.of("otpSent", otpSent, "email", userSecurity.getEmail()));
        }catch (BadCredentialsException e) {
            boolean otpSent = false;
            return ResponseEntity.ok(Map.of("otpSent",otpSent));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error During Admin Login.");
        }

    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerificationRequest otpVerificationRequest) {
        try {
            System.out.println(("Received OTP verification request for email: "+ otpVerificationRequest.getEmail()));

            boolean isValid = otpService.verifyOtp(otpVerificationRequest.getEmail(), otpVerificationRequest.getOtp());

            if (!isValid) {
                System.out.println(("OTP verification failed for email: "+ otpVerificationRequest.getEmail()));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired otp");
            }

            Optional<UserSecurity> optionalUser = securityUserRepository.findByEmail(otpVerificationRequest.getEmail());
            if (optionalUser.isEmpty()) {
                System.out.println(("User not found for email: "+ otpVerificationRequest.getEmail()));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }

            UserSecurity userSecurity = optionalUser.get();

            if (!"ADMIN".equals(userSecurity.getRole())) {
                System.out.println(("Non-admin user attempted to verify OTP: "+ userSecurity.getEmail()));
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied. Not an admin.");
            }

            String token = jwtTokenUtil.generateToken(userSecurity);
            System.out.println(("OTP verified successfully for admin: " + userSecurity.getEmail()));

            return ResponseEntity.ok(new AuthResponse(token, userSecurity.getEmail()));
        } catch (Exception e) {
            System.out.println(("Error during OTP verification" + e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error During Verify Otp: " + e.getMessage());
        }
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email ) {
        try{
            resetPasswordService.forgotPassword(email);

            return ResponseEntity.status(HttpStatus.OK).body("Reset Password Email Sent successfully");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error During Sending Reset Password Email: " + e.getMessage());
        }



    }


    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        try{
            resetPasswordService.resetPassword(resetPasswordRequest);
            return ResponseEntity.status(HttpStatus.OK).body("Password Reset Successful");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while resetting the password");
        }

    }



}
