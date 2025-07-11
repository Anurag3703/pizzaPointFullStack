package com.example.fullstack.database.model;

public enum DiscountType {
    FIXED_AMOUNT, // Flat discount (e.g., 500 HUF off)
    PERCENTAGE,   // Percentage discount (e.g., 10% off)
    FIRST_ORDER,  // Only for user's first order
    SEASONAL,
    FREE_DELIVERY// Seasonal promotions
}
