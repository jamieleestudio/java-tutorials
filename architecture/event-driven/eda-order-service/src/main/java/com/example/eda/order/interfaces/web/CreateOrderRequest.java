package com.example.eda.order.interfaces.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

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