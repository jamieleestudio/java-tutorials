package com.example.dist.payment.application.dto;

import java.math.BigDecimal;

/**
 * Application-layer output contract for the Payment context.
 */
public record PaymentDto(
        String paymentId,
        String orderId,
        BigDecimal amount,
        String status
) {
}