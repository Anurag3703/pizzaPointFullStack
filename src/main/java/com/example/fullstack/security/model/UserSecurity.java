package com.example.fullstack.security.model;


import com.example.fullstack.database.model.User;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;



@Entity(name = "SecurityUser")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSecurity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;
    private String role;
    private String phone;
    private boolean isMobileVerified;
    @OneToOne(mappedBy = "userSecurity",cascade = CascadeType.ALL)
    @ToString.Exclude
    private User user;
    private String resetToken;
    private LocalDateTime resetTokenExpiryTime;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority(role)
        );
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }





}
