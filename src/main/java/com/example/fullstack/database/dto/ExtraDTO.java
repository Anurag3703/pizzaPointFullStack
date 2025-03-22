package com.example.fullstack.database.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExtraDTO {
    private String id;
    private String name;
    private BigDecimal price;
}