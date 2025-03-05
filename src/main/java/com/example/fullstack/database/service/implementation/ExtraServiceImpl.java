package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.model.Extra;
import com.example.fullstack.database.repository.ExtraRepository;
import com.example.fullstack.database.service.ExtraService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExtraServiceImpl implements ExtraService {

    private final ExtraRepository extraRepository;
    public ExtraServiceImpl(ExtraRepository extraRepository) {
        this.extraRepository = extraRepository;
    }

    @Override
    public void saveExtra() {
        extraRepository.save(new Extra());

    }

    @Override
    public Optional<Extra> getExtraItemById(String extraId) {
        return extraRepository.findById(extraId);
    }

    @Override
    public List<Extra> getAllExtraItems() {
        return extraRepository.findAll();
    }
}
