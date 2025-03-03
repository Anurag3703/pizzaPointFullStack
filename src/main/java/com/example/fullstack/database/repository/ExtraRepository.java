package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.Extra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtraRepository extends JpaRepository<Extra, String> {

}
