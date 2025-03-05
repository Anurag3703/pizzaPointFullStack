package com.example.fullstack.database.service.implementation;


import com.example.fullstack.database.model.Payment;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentStripeService {
    private final String stripeApiKey = "sk_test_51Qz1pGJTIEO4OgUbDgcA34fro5zQzxb6mRTwHvdGETeMeyWpl3Jf9YwhtpB9jTiNvDfQ0WSCfWPoecbrFY1y6ZKq00mqtw1Ha0";

    public PaymentStripeService() {
        Stripe.apiKey = stripeApiKey;
    }

    public Payment createPaymentItent(Payment payment) throws StripeException {
        long amountInHuf = (long) (payment.getAmount() *100);

        PaymentIntentCreateParams param = PaymentIntentCreateParams.builder()
                .setAmount(amountInHuf)
                .setCurrency(payment.getCurrency())
                .build();


        PaymentIntent paymentIntent = PaymentIntent.create(param);

        payment.setPaymentIntentId(paymentIntent.getId());
        payment.setStatus(payment.getStatus());
        return payment;

    }

    public Payment getPaymentDetails(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        Payment payment = new Payment();
        payment.setPaymentIntentId(paymentIntent.getId());
        payment.setStatus(paymentIntent.getStatus());
        payment.setCurrency(paymentIntent.getCurrency());
        payment.setAmount(paymentIntent.getAmountReceived()/100.0);
        return payment;


    }
}
