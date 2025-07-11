package com.example.fullstack.security.repository;

import com.example.fullstack.security.model.UserSecurity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.management.relation.Role;
import java.util.List;
import java.util.Optional;

@Repository("securityUserRepository")
public interface SecurityUserRepository extends JpaRepository<UserSecurity, Long> {
    Optional<UserSecurity> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<UserSecurity> findByPhone(String phone);
    Optional<UserSecurity> findByResetToken(String resetToken);
    void deleteByResetToken(String resetToken);
    List<UserSecurity> findAllByRole(String role);
    @Query("SELECT u.email FROM SecurityUser u WHERE u.role = :role AND u.email IS NOT NULL")
    List<String> findEmailsByRole(@Param("role") String role);

}
