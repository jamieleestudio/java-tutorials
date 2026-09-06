package com.example.mmm.payment.interfaces.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Web request DTO for creating a payment.
 */
public record CreatePaymentRequest(
        @NotBlank String orderId,
        @NotNull @Positive BigDecimal amount
) {
}