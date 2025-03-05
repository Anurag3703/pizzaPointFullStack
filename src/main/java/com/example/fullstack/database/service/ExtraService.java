package com.example.fullstack.database.service;

import com.example.fullstack.database.model.Extra;

import java.util.List;
import java.util.Optional;

public interface ExtraService {
    void saveExtra();
    Optional<Extra> getExtraItemById(String extraId);
    List<Extra> getAllExtraItems();
}
