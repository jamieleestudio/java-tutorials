package com.example.mmm.payment.application.query;

/**
 * Query to get a payment by ID (read operation).
 */
public record GetPaymentByIdQuery(String paymentId) {
}