package com.example.ms.order.infrastructure.feign;

import com.example.ms.product.application.dto.ProductDto;
import com.example.ms.shared.BusinessRuleViolationException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductClientFallback implements ProductClient {

    @Override
    public ProductDto getProduct(String productId) {
        throw new BusinessRuleViolationException("PRODUCT_SERVICE_UNAVAILABLE",
                "Product service is currently unavailable, please retry later");
    }

    @Override
    public List<ProductDto> getProducts(String ids) {
        throw new BusinessRuleViolationException("PRODUCT_SERVICE_UNAVAILABLE",
                "Product service is currently unavailable, please retry later");
    }
}