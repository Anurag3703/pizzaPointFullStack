package com.example.fullstack.database.controller;

import com.example.fullstack.database.dto.PaymentRequestDTO;
import com.example.fullstack.database.dto.PaymentResponseDTO;
import com.example.fullstack.database.model.Payment;
import com.example.fullstack.database.service.implementation.PaymentServiceImpl;
import com.example.fullstack.database.service.implementation.PaymentStripeService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentStripeService paymentStripeService;
    private final PaymentServiceImpl   paymentServiceImpl;

    public PaymentController(PaymentStripeService paymentStripeService, PaymentServiceImpl paymentServiceImpl) {
        this.paymentStripeService = paymentStripeService;
        this.paymentServiceImpl = paymentServiceImpl;
    }


    @PostMapping("/create")
    public ResponseEntity<Payment> createPaymentIntent(@RequestBody Payment payment) throws StripeException {
       try {
           Payment createPayment = paymentStripeService.createPaymentIntent(payment);
           return ResponseEntity.ok(createPayment);
       }
       catch (StripeException e) {
            return ResponseEntity.status(500).body(null);
       }
    }

    @GetMapping("/retrieve/{paymentIntentId}")
    public ResponseEntity<Payment> retrievePayment(@PathVariable String paymentIntentId) {
        try {
            Payment payment = paymentStripeService.getPaymentDetails(paymentIntentId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null); // Handle error gracefully
        }
    }

    @PostMapping("/charge")
    public ResponseEntity<PaymentResponseDTO> chargeCard(@Valid @RequestBody PaymentRequestDTO paymentRequestDTO) {
        try {
            if (paymentRequestDTO.getCardToken() == null || paymentRequestDTO.getCardToken().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new PaymentResponseDTO("FAILED", "Card token is required"));
            }
            if (paymentRequestDTO.getAmount() == null || paymentRequestDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(new PaymentResponseDTO("FAILED", "Invalid amount"));
            }
            if (!"HUF".equals(paymentRequestDTO.getCurrency())) {
                return ResponseEntity.badRequest().body(new PaymentResponseDTO("FAILED", "Only HUF is supported"));
            }

            PaymentResponseDTO response = paymentServiceImpl.chargeCard(
                    paymentRequestDTO.getCardToken(),
                    paymentRequestDTO.getAmount()
            );
            return ResponseEntity.status(response.getStatus().equals("COMPLETED") ? 200 : 400).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new PaymentResponseDTO("FAILED", "Error charging card: " + e.getMessage()));
        }
    }


    @GetMapping("/status/{orderId}")
    public ResponseEntity<PaymentResponseDTO> getPaymentStatus(@PathVariable String orderId) {
        try {
            PaymentResponseDTO response = paymentServiceImpl.getPaymentStatus(orderId);
            return ResponseEntity.status(response.getStatus().equals("FAILED") ? 400 : 200).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new PaymentResponseDTO("FAILED", "Error retrieving payment status: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/refund/{orderId}")
    public ResponseEntity<PaymentResponseDTO> refundPayment(@PathVariable String orderId, @Valid @RequestBody PaymentRequestDTO paymentRequestDTO) {
        try {
            if (paymentRequestDTO.getAmount() == null || paymentRequestDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(new PaymentResponseDTO("FAILED", "Invalid refund amount"));
            }
            if (!"HUF".equals(paymentRequestDTO.getCurrency())) {
                return ResponseEntity.badRequest().body(new PaymentResponseDTO("FAILED", "Only HUF is supported"));
            }

            PaymentResponseDTO response = paymentServiceImpl.refundPayment(orderId, paymentRequestDTO.getAmount());
            return ResponseEntity.status(response.getStatus().equals("COMPLETED") ? 200 : 400).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new PaymentResponseDTO("FAILED", "Error processing refund: " + e.getMessage()));
        }
    }

}
