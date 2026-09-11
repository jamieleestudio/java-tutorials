package com.example.mmm.product.application.dto;

import java.math.BigDecimal;

/**
 * Application-layer output contract for the Product context.
 */
public record ProductDto(
        String productId,
        String name,
        BigDecimal price,
        int stock
) {
}