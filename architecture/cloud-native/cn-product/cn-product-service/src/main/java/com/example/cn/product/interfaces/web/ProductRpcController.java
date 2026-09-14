package com.example.cn.product.interfaces.web;

import com.example.cn.product.application.ProductService;
import com.example.cn.product.application.dto.ProductDto;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * Internal RPC contract exposed by the product service for the order service.
 */
@RestController
@RequestMapping("/rpc/products")
public class ProductRpcController {

    private final ProductService productService;

    public ProductRpcController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{productId}")
    public ProductDto getProduct(@PathVariable String productId) {
        return productService.getProduct(productId);
    }

    @GetMapping
    public List<ProductDto> getProducts(@RequestParam String ids) {
        return productService.getProducts(Arrays.asList(ids.split(",")));
    }
}