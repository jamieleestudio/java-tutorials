package com.example.mmm.payment.interfaces.web;

import com.example.mmm.payment.application.dto.PaymentDto;

import java.math.BigDecimal;

/**
 * Web response DTO for payment — interfaces layer only.
 */
public record PaymentResponse(
        String paymentId,
        String orderId,
        BigDecimal amount,
        String status
) {
    public static PaymentResponse from(PaymentDto dto) {
        return new PaymentResponse(dto.paymentId(), dto.orderId(), dto.amount(), dto.status());
    }
}