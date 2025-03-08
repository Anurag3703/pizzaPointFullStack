package com.example.fullstack.database.model;


import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    private String id;
    private String paymentIntentId;
    private Double amount;
    private String currency;
    private String status;



}
