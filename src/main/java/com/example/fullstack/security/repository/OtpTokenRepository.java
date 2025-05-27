package com.example.fullstack.security.repository;


import com.example.fullstack.security.model.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findByEmailAndToken(String email, String token);
    void deleteByEmail(String email);
    List<OtpToken> findByExpiryTimeBefore(LocalDateTime time);

}
