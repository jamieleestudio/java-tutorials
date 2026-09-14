package com.example.cn.product.interfaces.web;

import com.example.cn.product.application.ProductAppService;
import com.example.cn.product.application.command.CreateProductCommand;
import com.example.cn.product.application.dto.ProductDto;
import com.example.cn.product.application.query.GetAllProductsQuery;
import com.example.cn.product.application.query.GetProductByIdQuery;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductAppService productService;

    public ProductController(ProductAppService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        CreateProductCommand command = new CreateProductCommand(
                request.name(), request.price(), request.stock()
        );
        ProductDto dto = productService.createProduct(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponse.from(dto));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable String productId) {
        ProductDto dto = productService.getProductById(new GetProductByIdQuery(productId));
        return ResponseEntity.ok(ProductResponse.from(dto));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> responses = productService.getAllProducts(new GetAllProductsQuery()).stream()
                .map(ProductResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }
}