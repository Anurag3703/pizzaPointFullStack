package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
