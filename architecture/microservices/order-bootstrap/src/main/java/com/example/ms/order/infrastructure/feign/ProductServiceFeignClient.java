package com.example.ms.order.infrastructure.feign;

import com.example.ms.product.application.ProductService;
import com.example.ms.product.application.dto.ProductDto;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ③→⑤ evolution: ProductService implementation swapped to Feign client.
 */
@Component
public class ProductServiceFeignClient implements ProductService {

    private final ProductClient productClient;

    public ProductServiceFeignClient(ProductClient productClient) {
        this.productClient = productClient;
    }

    @Override
    public ProductDto getProduct(String productId) {
        return productClient.getProduct(productId);
    }

    @Override
    public List<ProductDto> getProducts(List<String> productIds) {
        return productClient.getProducts(String.join(",", productIds));
    }
}