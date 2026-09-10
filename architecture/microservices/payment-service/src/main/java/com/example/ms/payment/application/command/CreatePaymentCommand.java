package com.example.ms.payment.application.command;

import java.math.BigDecimal;

/**
 * Command to create a payment (write operation).
 */
public record CreatePaymentCommand(String orderId, BigDecimal amount) {
}