package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.Extra;
import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExtraRepository extends JpaRepository<Extra, String> {
    @Nonnull
    Optional<Extra> findById(@Nonnull String extraItemId);

}
