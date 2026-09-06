package com.example.cn.order.infrastructure.rpc;

import com.example.cn.product.application.ProductService;
import com.example.cn.product.application.dto.ProductDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.function.Supplier;

/**
 * ②→③ evolution point: ProductService implementation swapped to RPC client.
 */
@Component
public class ProductServiceRpcClient implements ProductService {

    private static final int MAX_ATTEMPTS = 2;

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
        return withRetry(() -> restClient.get()
                .uri("/rpc/products/{productId}", productId)
                .retrieve()
                .body(ProductDto.class));
    }

    @Override
    public List<ProductDto> getProducts(List<String> productIds) {
        return withRetry(() -> restClient.get()
                .uri(uri -> uri.path("/rpc/products")
                        .queryParam("ids", String.join(",", productIds))
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProductDto>>() {
                }));
    }

    private <T> T withRetry(Supplier<T> call) {
        ResourceAccessException last = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return call.get();
            } catch (ResourceAccessException e) {
                last = e;
            }
        }
        throw last;
    }
}