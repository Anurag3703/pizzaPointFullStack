package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.model.Extra;
import com.example.fullstack.database.repository.ExtraRepository;
import com.example.fullstack.database.service.ExtraService;

public class ExtraServiceImpl implements ExtraService {

    private final ExtraRepository extraRepository;
    public ExtraServiceImpl(ExtraRepository extraRepository) {
        this.extraRepository = extraRepository;
    }

    @Override
    public void saveExtra() {
        extraRepository.save(new Extra());

    }
}
