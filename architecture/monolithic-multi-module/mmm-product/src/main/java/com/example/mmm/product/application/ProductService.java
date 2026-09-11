package com.example.mmm.product.application;

import com.example.mmm.product.application.command.CreateProductCommand;
import com.example.mmm.product.application.command.DeductStockCommand;
import com.example.mmm.product.application.dto.ProductDto;
import com.example.mmm.product.application.query.GetAllProductsQuery;
import com.example.mmm.product.application.query.GetProductByIdQuery;

import java.util.List;

/**
 * Application service contract for the Product context.
 * Provider-defined interface — consumed by this context's interfaces layer
 * and by the Order context (cross-context calls).
 */
public interface ProductService {

    ProductDto createProduct(CreateProductCommand command);

    void deductStock(DeductStockCommand command);

    ProductDto getProductById(GetProductByIdQuery query);

    List<ProductDto> getAllProducts(GetAllProductsQuery query);

    // --- Cross-context methods (consumed by Order context) ---

    ProductDto getProduct(String productId);

    List<ProductDto> getProducts(List<String> productIds);
}