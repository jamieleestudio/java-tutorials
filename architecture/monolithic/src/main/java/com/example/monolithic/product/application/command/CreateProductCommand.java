package com.example.monolithic.product.application.command;

import java.math.BigDecimal;

/**
 * Command to create a new product (write operation).
 */
public record CreateProductCommand(
        String name,
        BigDecimal price,
        int stock
) {
}