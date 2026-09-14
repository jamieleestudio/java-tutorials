package com.example.eda.product.interfaces.web;

import com.example.eda.product.application.ProductAppService;
import com.example.eda.product.application.command.CreateProductCommand;
import com.example.eda.product.application.dto.ProductDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductAppService productAppService;

    public ProductController(ProductAppService productAppService) {
        this.productAppService = productAppService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductDto dto = productAppService.createProduct(new CreateProductCommand(
                request.name(), request.price(), request.stock()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponse.from(dto));
    }
}