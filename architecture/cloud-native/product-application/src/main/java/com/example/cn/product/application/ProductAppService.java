package com.example.cn.product.application;

import com.example.cn.product.application.command.CreateProductCommand;
import com.example.cn.product.application.command.DeductStockCommand;
import com.example.cn.product.application.dto.ProductDto;
import com.example.cn.product.application.query.GetAllProductsQuery;
import com.example.cn.product.application.query.GetProductByIdQuery;

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