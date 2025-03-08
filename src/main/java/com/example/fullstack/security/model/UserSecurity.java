package com.example.fullstack.security.model;


import com.example.fullstack.database.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;



@Entity(name = "SecurityUser")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSecurity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String email;
    private String password;
    private String role;
    private String phone;
    private boolean isMobileVerified;
    @OneToOne(mappedBy = "userSecurity",cascade = CascadeType.ALL)
    @JsonManagedReference //backward side of the relationship
    @ToString.Exclude
    private User user;

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
