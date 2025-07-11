package com.example.fullstack.database.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtraDTO {
    private String id;
    private String name;
    private BigDecimal price;
}