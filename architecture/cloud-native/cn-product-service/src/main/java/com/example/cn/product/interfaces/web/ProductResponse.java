package com.example.cn.product.interfaces.web;

import com.example.cn.product.application.dto.ProductDto;

import java.math.BigDecimal;

/**
 * Web response DTO for product — interfaces layer only.
 */
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