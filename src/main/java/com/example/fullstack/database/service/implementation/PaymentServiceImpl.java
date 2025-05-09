package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.dto.PaymentResponseDTO;
import com.example.fullstack.database.service.PaymentService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


@Service
public class PaymentServiceImpl implements PaymentService {
    @Override
    public PaymentResponseDTO chargeCard(String cardToken, BigDecimal amount) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://merchant.revolut.com/api/1.0/pay";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("sk_VJksHGJJpeSAeCH9qG7xQ53OH_rj7iXZSFkTEve5MNd7z7teoluZr6ycldB1kcbq");

        Map<String,Object> body = new HashMap<>();
        body.put("cardToken", cardToken);
        body.put("amount", amount);
        body.put("currency", "EUR");
        body.put("capture",true);

        HttpEntity<Map<String,Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if(response.getStatusCode() == HttpStatus.OK && "COMPLETED".equals(response.getBody().get("status"))) {
            return new PaymentResponseDTO("COMPLETED","Payment Successful");
        }else{
            return new PaymentResponseDTO("FAILED","Payment Failed");
        }


    }
}
