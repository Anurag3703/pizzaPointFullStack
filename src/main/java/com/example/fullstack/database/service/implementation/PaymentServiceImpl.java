package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.dto.PaymentResponseDTO;
import com.example.fullstack.database.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Service

public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Value("${revolut.api.key}")
    private String revolutApiKey;

    @Value("${revolut.api.url:https://merchant.revolut.com/api/1.0/orders}")
    private String revolutApiUrl;
    @Override
    public PaymentResponseDTO chargeCard(String cardToken, BigDecimal amount) {
        logger.info("Attempting to charge {} Huf with card token {}", amount, cardToken);
        try{
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(revolutApiKey);
            headers.add("Idempotency-Key", UUID.randomUUID().toString());

            long amountInHuf = amount.longValue();

            Map<String,Object>  body = new HashMap<>();
            body.put("amount",amountInHuf);
            body.put("currency","HUF");
            body.put("description" , "Food Order Payment start");
            body.put("capture_mode", "AUTOMATIC");

            Map<String, Object> paymentMethod = new HashMap<>();
            paymentMethod.put("type","card");
            paymentMethod.put("card_token",cardToken);
            body.put("payment_method",paymentMethod);
            body.put("merchant_order_ext_ref",UUID.randomUUID().toString());

            HttpEntity<Map<String,Object>> request = new HttpEntity<>(body,headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(revolutApiUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();

                if (responseBody != null) {
                    String state = (String) responseBody.get("state");
                    String orderId = (String) responseBody.get("id");

                    if ("COMPLETED".equals(state) || "PROCESSING".equals(state)) {
                        logger.info("Payment successful: orderId={}, state={}", orderId, state);
                        return new PaymentResponseDTO("COMPLETED", "Payment processed successfully", orderId);
                    } else if ("FAILED".equals(state)) {
                        logger.warn("Payment failed: orderId={}, state={}", orderId, state);
                        return new PaymentResponseDTO("FAILED", "Payment failed", orderId);
                    } else {
                        logger.info("Payment pending: orderId={}, state={}", orderId, state);
                        return new PaymentResponseDTO("PENDING", "Payment is being processed", orderId);
                    }
                }
            }
            logger.error("Payment processing failed: no response body");
            return new PaymentResponseDTO("FAILED", "Payment processing failed");

        }catch (HttpClientErrorException e){
            String errorMessage = getString(e);
            logger.error("Payment error: {}", errorMessage);
            return new PaymentResponseDTO("FAILED", errorMessage);

        } catch (Exception e) {
            logger.error("Payment processing error: {}", e.getMessage());
            return new PaymentResponseDTO("FAILED", "Payment processing error: " + e.getMessage());
        }
    }

    private static String getString(HttpClientErrorException e) {
        String errorMessage;
        if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
            errorMessage = "Invalid payment details";
        } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            errorMessage = "Payment authentication failed";
        } else if (e.getStatusCode() == HttpStatus.PAYMENT_REQUIRED) {
            errorMessage = "Insufficient funds";
        } else {
            errorMessage = "Payment failed: " + e.getMessage();
        }
        return errorMessage;
    }


    @Override
    public PaymentResponseDTO getPaymentStatus(String orderId) {
        logger.info("Checking payment status for orderId={}", orderId);
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(revolutApiKey);
            HttpEntity<?> request = new HttpEntity<>(headers);
            String statusUrl = revolutApiUrl + "/" + orderId;

            ResponseEntity<Map> response = restTemplate.exchange(statusUrl, HttpMethod.GET, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String state = (String) response.getBody().get("state");
                String message = switch (state) {
                    case "COMPLETED" -> "Payment completed successfully";
                    case "PROCESSING" -> "Payment is being processed";
                    case "FAILED" -> "Payment failed";
                    case "PENDING" -> "Payment order is pending";
                    default -> "Unknown payment status";
                };
                logger.info("Payment status: orderId={}, state={}", orderId, state);
                return new PaymentResponseDTO(state, message, orderId);
            }
            logger.error("Unable to retrieve payment status: orderId={}", orderId);
            return new PaymentResponseDTO("FAILED", "Unable to retrieve payment status");
        } catch (Exception e) {
            logger.error("Error retrieving payment status: orderId={}, error={}", orderId, e.getMessage());
            return new PaymentResponseDTO("FAILED", "Error retrieving payment status: " + e.getMessage());
        }
    }

    @Override
    public PaymentResponseDTO refundPayment(String orderId, BigDecimal amount) {
        logger.info("Attempting to refund {} HUF for orderId={}", amount, orderId);
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(revolutApiKey);
            headers.add("Idempotency-Key", UUID.randomUUID().toString()); // Prevent duplicate refunds

            long amountInHUF = amount.longValue(); // HUF has no minor units

            Map<String, Object> body = new HashMap<>();
            body.put("amount", amountInHUF);
            body.put("currency", "HUF");
            body.put("merchant_refunded_reason", "Order cancelled or refund requested");

            String refundUrl = revolutApiUrl + "/" + orderId + "/refund";
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(refundUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null) {
                    String state = (String) responseBody.get("state");
                    if ("COMPLETED".equals(state)) {
                        logger.info("Refund successful: orderId={}", orderId);
                        return new PaymentResponseDTO("COMPLETED", "Refund processed successfully", orderId);
                    }
                }
            }

            logger.error("Refund failed: orderId={}", orderId);
            return new PaymentResponseDTO("FAILED", "Refund processing failed");

        } catch (HttpClientErrorException e) {
            String errorMessage = switch (e.getStatusCode().value()) {
                case 400 -> "Invalid refund details";
                case 401 -> "Refund authentication failed";
                case 403 -> "Refund not allowed";
                case 404 -> "Payment order not found";
                default -> "Refund failed: " + e.getMessage();
            };
            logger.error("Refund error: orderId={}, status={}, message={}", orderId, e.getStatusCode(), errorMessage);
            return new PaymentResponseDTO("FAILED", errorMessage);

        } catch (Exception e) {
            logger.error("Refund processing error: orderId={}, error={}", orderId, e.getMessage());
            return new PaymentResponseDTO("FAILED", "Unexpected error during refund processing");
        }
    }
}
