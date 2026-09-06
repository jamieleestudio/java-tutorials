package com.example.eda.product.interfaces.web;

import com.example.eda.product.application.ProductAppService;
import com.example.eda.product.application.dto.ProductDto;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * Sync catalog READ for order-service (hybrid: sync read + async write).
 */
@RestController
@RequestMapping("/rpc/products")
public class ProductRpcController {

    private final ProductAppService productAppService;

    public ProductRpcController(ProductAppService productAppService) {
        this.productAppService = productAppService;
    }

    @GetMapping("/{productId}")
    public ProductDto getProduct(@PathVariable String productId) {
        return productAppService.getProduct(productId);
    }

    @GetMapping
    public List<ProductDto> getProducts(@RequestParam String ids) {
        return productAppService.getProducts(Arrays.asList(ids.split(",")));
    }
}