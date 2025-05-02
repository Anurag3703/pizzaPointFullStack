package com.example.fullstack.config;

import com.example.fullstack.database.model.User;
import com.example.fullstack.database.repository.UserRepository;
import com.example.fullstack.security.model.UserSecurity;
import com.example.fullstack.security.repository.SecurityUserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component

public class AdminInitializer  implements CommandLineRunner {

    @Value("${ADMIN_EMAIL:}")
    private String email;

    @Value("${ADMIN_PASSWORD:}")
    private String password;

    @Value("${ADMIN_NAME:Admin}")
    private String name;

    @Value("${ADMIN_ADDRESS:N/A}")
    private String address;

    @Value("${ADMIN_PHONE:N/A}")
    private String phone;

    private final SecurityUserRepository securityUserRepository;

    public AdminInitializer(SecurityUserRepository securityUserRepository, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.securityUserRepository = securityUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (email.isEmpty() || password.isEmpty()) {
            System.out.println("Admin email or password not provided. Skipping admin creation.");
            return;
        }

        if (!securityUserRepository.existsByEmail(email)) {
            UserSecurity adminSecurity = new UserSecurity();
            adminSecurity.setEmail(email);
            adminSecurity.setPassword(passwordEncoder.encode(password));
            adminSecurity.setRole("ADMIN");

            User adminUser = new User();
            adminUser.setEmail(email);
            adminUser.setName(name);
            adminUser.setAddress(address);
            adminUser.setPhone(phone);

            adminUser.setUserSecurity(adminSecurity);
            adminSecurity.setUser(adminUser);

            userRepository.save(adminUser);
            System.out.println("Admin user created successfully.");
        } else {
            System.out.println("Admin user already exists.");
        }
    }
}
