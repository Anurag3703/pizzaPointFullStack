package com.example.fullstack.database.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestDTO {
    private String cardToken;
    private BigDecimal amount;
    private String description;
    private String orderId;
    private String currency = "HUF";

}
