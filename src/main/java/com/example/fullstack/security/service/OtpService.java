package com.example.fullstack.security.service;

import com.example.fullstack.security.model.OtpToken;
import com.example.fullstack.security.repository.OtpTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;

    public OtpService(OtpTokenRepository otpTokenRepository) {
        this.otpTokenRepository = otpTokenRepository;
    }

    @Transactional
    public String generateOtp(String email) {
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
//        String hashOtp = hashOtp(otp);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        otpTokenRepository.deleteByEmail(email);
        OtpToken otpToken = new OtpToken();
        otpToken.setEmail(email);
        otpToken.setExpiryTime(expiresAt);
        otpToken.setToken(otp);
        otpTokenRepository.save(otpToken);

        return otp;

    }

    @Transactional
    public  boolean verifyOtp(String email, String  otp) {

        Optional<OtpToken> optionalOtpToken = otpTokenRepository.findByEmailAndToken(email, otp);
        if (optionalOtpToken.isPresent()) {
            if(optionalOtpToken.get().getToken().equals(otp) && optionalOtpToken.get().getExpiryTime().isAfter(LocalDateTime.now())) {
                otpTokenRepository.deleteByEmail(email);
                return true;
            }
            otpTokenRepository.deleteByEmail(email);

        }
        return false;
    }

    public String hashOtp(String otp) {
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(otp.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported",e);
        }
    }
}
