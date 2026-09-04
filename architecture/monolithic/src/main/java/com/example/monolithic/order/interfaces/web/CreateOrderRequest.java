package com.example.monolithic.order.interfaces.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for creating an order.
 * Web-specific, lives in interfaces layer only.
 */
public record CreateOrderRequest(
        @NotBlank String customerId,
        @NotEmpty @Valid List<OrderLineRequest> lines
) {
    public record OrderLineRequest(
            @NotBlank String productId,
            @Positive int quantity
    ) {
    }
}