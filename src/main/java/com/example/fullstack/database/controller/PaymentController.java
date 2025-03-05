package com.example.fullstack.database.controller;

import com.example.fullstack.database.model.Payment;
import com.example.fullstack.database.service.implementation.PaymentStripeService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentStripeService paymentStripeService;

    public PaymentController(PaymentStripeService paymentStripeService) {
        this.paymentStripeService = paymentStripeService;
    }


    @PostMapping("/create")
    public ResponseEntity<Payment> createPaymentIntent(@RequestBody Payment payment) throws StripeException {
       try {
           Payment createPayment = paymentStripeService.createPaymentItent(payment);
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
}
