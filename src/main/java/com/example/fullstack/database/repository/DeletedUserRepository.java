package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.DeletedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeletedUserRepository extends JpaRepository<DeletedUser, Long> {
    List<DeletedUser> findByPhone(String phone);

}
