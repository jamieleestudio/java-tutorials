package com.example.monolithic.product.application.query;

/**
 * Query to get a product by ID (read operation).
 */
public record GetProductByIdQuery(String productId) {
}