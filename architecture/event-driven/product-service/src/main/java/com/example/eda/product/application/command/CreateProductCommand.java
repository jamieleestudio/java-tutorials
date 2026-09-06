package com.example.eda.product.application.command;

import java.math.BigDecimal;

public record CreateProductCommand(
        String name,
        BigDecimal price,
        int stock
) {
}