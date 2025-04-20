package com.example.fullstack.config;

import com.example.fullstack.database.model.User;
import com.example.fullstack.database.repository.UserRepository;
import com.example.fullstack.security.model.UserSecurity;
import com.example.fullstack.security.repository.SecurityUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AdminInitializer  implements CommandLineRunner {

    private final SecurityUserRepository securityUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Existing admin check logic
        if(!securityUserRepository.existsByEmail("anuragtiwari3703@gmail.com")){
            UserSecurity adminSecurity = new UserSecurity();
            adminSecurity.setEmail("anuragtiwari3703@gmail.com");
            adminSecurity.setPassword(passwordEncoder.encode("Anu_369rag"));
            adminSecurity.setRole("ADMIN");


            User adminUser = new User();
            adminUser.setEmail("anuragtiwari3703@gmail.com");
            adminUser.setName("Anurag Tiwari");
            adminUser.setAddress("Petofi ter 6");
            adminUser.setPhone("+36705599768");


            adminUser.setUserSecurity(adminSecurity);
            adminSecurity.setUser(adminUser);
            userRepository.save(adminUser);


            System.out.println("Admin user created successfully.");

        }else {
            System.out.println("Admin user already exists.");
        }

    }
}
