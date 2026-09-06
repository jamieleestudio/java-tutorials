package com.example.eda.product.interfaces.web;

import com.example.eda.product.application.dto.ProductDto;

import java.math.BigDecimal;

public record ProductResponse(
        String productId,
        String name,
        BigDecimal price,
        int stock
) {
    public static ProductResponse from(ProductDto dto) {
        return new ProductResponse(dto.productId(), dto.name(), dto.price(), dto.stock());
    }
}