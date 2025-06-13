package com.example.fullstack.database.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseDTO {
    private String status;
    private String message;
    private String transactionId;

    public PaymentResponseDTO(String failed, String paymentProcessingFailed) {
    }
}
