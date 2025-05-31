package com.example.fullstack.security.service;


import com.example.fullstack.security.model.ResetPasswordRequest;
import com.example.fullstack.security.model.UserSecurity;
import com.example.fullstack.security.repository.SecurityUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
public class ResetPasswordService {

    private final SecurityUserRepository securityUserRepository;
    private final SecurityEmailService securityEmailService;
    private final PasswordEncoder passwordEncoder;
    public ResetPasswordService(SecurityUserRepository securityUserRepository, SecurityEmailService securityEmailService, PasswordEncoder passwordEncoder) {
        this.securityUserRepository = securityUserRepository;
        this.securityEmailService = securityEmailService;
        this.passwordEncoder = passwordEncoder;
    }


    @Transactional
    public void forgotPassword(String email) {

        UserSecurity userSecurity = securityUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User doesn't exist"));

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(10);
        userSecurity.setResetToken(token);
        userSecurity.setResetTokenExpiryTime(expiryTime);
        String url = "https://pizza-point-k53k.vercel.app/reset-password?token=" + token;
        securityEmailService.resetPasswordEmail(userSecurity.getEmail(), url);
        System.out.println("Email Sent Successfully");
    }


    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        UserSecurity userSecurity = securityUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User doesn't exist"));

        if (userSecurity.getResetTokenExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token has expired");
        }


        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }


        if (passwordEncoder.matches(request.getPassword(), userSecurity.getPassword())) {
            throw new IllegalArgumentException("Same password used. Please use a new password.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        userSecurity.setPassword(encodedPassword);
        userSecurity.setResetToken(null); // Invalidate token
        userSecurity.setResetTokenExpiryTime(null);
        securityUserRepository.save(userSecurity);

    }
}

