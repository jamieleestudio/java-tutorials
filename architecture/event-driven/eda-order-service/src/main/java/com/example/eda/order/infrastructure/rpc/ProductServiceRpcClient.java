package com.example.eda.order.infrastructure.rpc;

import com.example.eda.product.application.ProductService;
import com.example.eda.product.application.dto.ProductDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Sync catalog READ from product-service (pragmatic hybrid: sync read + async write).
 */
@Component
public class ProductServiceRpcClient implements ProductService {

    private final RestClient restClient;

    public ProductServiceRpcClient(@Value("${product.service.base-url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    @Override
    public ProductDto getProduct(String productId) {
        return restClient.get()
                .uri("/rpc/products/{productId}", productId)
                .retrieve()
                .body(ProductDto.class);
    }

    @Override
    public List<ProductDto> getProducts(List<String> productIds) {
        return restClient.get()
                .uri(uri -> uri.path("/rpc/products")
                        .queryParam("ids", String.join(",", productIds))
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProductDto>>() {
                });
    }
}