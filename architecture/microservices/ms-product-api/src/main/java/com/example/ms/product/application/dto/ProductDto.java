package com.example.ms.product.application.dto;

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