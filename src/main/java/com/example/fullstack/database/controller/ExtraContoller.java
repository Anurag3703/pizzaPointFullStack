package com.example.fullstack.database.controller;


import com.example.fullstack.database.model.Extra;
import com.example.fullstack.database.service.ExtraService;
import com.example.fullstack.database.service.implementation.ExtraServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/extras")
public class ExtraContoller {
    private final ExtraServiceImpl extraServiceImpl;
    public ExtraContoller(ExtraServiceImpl extraServiceImpl) {
        this.extraServiceImpl = extraServiceImpl;
    }

    @GetMapping("/get/{extraId}")
    public Optional<Extra> getExtraItemById(@PathVariable String extraId) {
        return extraServiceImpl.getExtraItemById(extraId);

    }

    @GetMapping("/get/all")
    public List<Extra> getAllExtraItems() {
        return extraServiceImpl.getAllExtraItems();
    }
}
