package com.example.fullstack.security.repository;

import com.example.fullstack.security.model.UserSecurity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("securityUserRepository")
public interface SecurityUserRepository extends JpaRepository<UserSecurity, Long> {
    Optional<UserSecurity> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<UserSecurity> findByPhone(String phone);
}
