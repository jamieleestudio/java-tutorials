package com.example.cn.payment.application.command;

/**
 * Command to process a payment (write operation).
 */
public record ProcessPaymentCommand(String paymentId) {
}