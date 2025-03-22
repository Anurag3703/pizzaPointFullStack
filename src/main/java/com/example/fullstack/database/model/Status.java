package com.example.fullstack.database.model;

public enum Status {
    PENDING,          // Order has been placed, but not yet processed
    PLACED,
    PREPARING,        // Pizza is being prepared
    READY_FOR_PICKUP, // Pizza is ready to be delivered or picked up
    OUT_FOR_DELIVERY, // Pizza is out for delivery to the customer
    DELIVERED,        // Pizza has been delivered successfully
    COMPLETED,        // Order is completed (could be used to mark final status after delivery)
    CANCELLED,        // Order has been cancelled by the customer or system
    FAILED;



    public String toUpperCase() {
         return this.name().toUpperCase();
    }
}
