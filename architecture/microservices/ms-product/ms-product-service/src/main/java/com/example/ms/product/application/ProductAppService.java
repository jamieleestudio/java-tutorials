package com.example.ms.product.application;

import com.example.ms.product.application.command.CreateProductCommand;
import com.example.ms.product.application.command.DeductStockCommand;
import com.example.ms.product.application.dto.ProductDto;
import com.example.ms.product.application.query.GetAllProductsQuery;
import com.example.ms.product.application.query.GetProductByIdQuery;

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