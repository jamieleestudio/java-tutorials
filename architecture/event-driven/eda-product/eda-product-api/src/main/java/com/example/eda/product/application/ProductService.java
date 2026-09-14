package com.example.eda.product.application;

import com.example.eda.product.application.dto.ProductDto;

import java.util.List;

/**
 * Product provider contract — CROSS-CONTEXT methods only.
 * Consumed remotely by the order service.
 */
public interface ProductService {

    ProductDto getProduct(String productId);

    List<ProductDto> getProducts(List<String> productIds);
}