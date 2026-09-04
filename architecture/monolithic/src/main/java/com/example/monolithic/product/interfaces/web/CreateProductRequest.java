package com.example.monolithic.product.interfaces.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank String name,
        @NotNull @Positive BigDecimal price,
        @PositiveOrZero int stock
) {
}