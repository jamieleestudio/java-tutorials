package com.example.ms.order.infrastructure.feign;

import com.example.ms.product.application.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "product-service", path = "/rpc/products", fallback = ProductClientFallback.class)
public interface ProductClient {

    @GetMapping("/{productId}")
    ProductDto getProduct(@PathVariable("productId") String productId);

    @GetMapping
    List<ProductDto> getProducts(@RequestParam("ids") String ids);
}