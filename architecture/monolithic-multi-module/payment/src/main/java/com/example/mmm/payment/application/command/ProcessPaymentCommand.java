package com.example.mmm.payment.application.command;

/**
 * Command to process a payment (write operation).
 */
public record ProcessPaymentCommand(String paymentId) {
}