package com.example.fullstack.database.service;

import com.example.fullstack.database.dto.PaymentResponseDTO;

import java.math.BigDecimal;

public interface PaymentService {

    PaymentResponseDTO chargeCard(String cardToken, BigDecimal amount);
}
