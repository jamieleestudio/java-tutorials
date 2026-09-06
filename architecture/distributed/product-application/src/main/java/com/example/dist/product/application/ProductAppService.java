package com.example.dist.product.application;

import com.example.dist.product.application.command.CreateProductCommand;
import com.example.dist.product.application.command.DeductStockCommand;
import com.example.dist.product.application.dto.ProductDto;
import com.example.dist.product.application.query.GetAllProductsQuery;
import com.example.dist.product.application.query.GetProductByIdQuery;

import java.util.List;

/**
 * Product OWN use-case contract — extends the cross-context {@link ProductService}.
 */
public interface ProductAppService extends ProductService {

    ProductDto createProduct(CreateProductCommand command);

    void deductStock(DeductStockCommand command);

    ProductDto getProductById(GetProductByIdQuery query);

    List<ProductDto> getAllProducts(GetAllProductsQuery query);
}