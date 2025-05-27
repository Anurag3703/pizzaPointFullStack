package com.example.fullstack.config;

import com.example.fullstack.database.model.User;
import com.example.fullstack.database.repository.UserRepository;
import com.example.fullstack.security.model.UserSecurity;
import com.example.fullstack.security.repository.SecurityUserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component

public class AdminInitializer  implements CommandLineRunner {

    @Value("#{'${admin.emails:}'.split(',')}")
    private List<String> emails;

    @Value("#{'${admin.passwords:}'.split(',')}")
    private List<String> passwords;

    @Value("#{'${admin.names:}'.split(',')}")
    private List<String > names;

    @Value("#{'${admin.addresses:}'.split(',')}")
    private List<String> addresses;

    @Value("#{'${admin.phones:}'.split(',')}")
    private List<String> phones;

    private final SecurityUserRepository securityUserRepository;

    public AdminInitializer(SecurityUserRepository securityUserRepository, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.securityUserRepository = securityUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (emails.isEmpty() || passwords.isEmpty()) {
            System.out.println("Admin emails or passwords not provided. Skipping admin creation.");
            return;
        }

        for (int i = 0; i < emails.size(); i++) {
            String email = emails.get(i).trim();
            String password = passwords.size() > i ? passwords.get(i).trim() : "";
            String name = names.size() > i ? names.get(i).trim() : "Admin";
            String phone = phones.size() > i ? phones.get(i).trim() : "N/A";
            String address = addresses.size() > i ? addresses.get(i).trim() : "N/A";

            if (!securityUserRepository.existsByEmail(email)) {
                UserSecurity adminSecurity = new UserSecurity();
                adminSecurity.setEmail(email);
                adminSecurity.setPassword(passwordEncoder.encode(password));
                adminSecurity.setPhone(phone);
                adminSecurity.setRole("ADMIN");

                User adminUser = new User();
                adminUser.setEmail(email);
                adminUser.setName(name);
                adminUser.setAddress(address);
                adminUser.setPhone(phone);

                adminUser.setUserSecurity(adminSecurity);
                adminSecurity.setUser(adminUser);

                userRepository.save(adminUser);
                System.out.println("Admin user created successfully: " + email);
            } else {
                System.out.println("Admin user already exists: " + email);
            }
        }
    }
}
