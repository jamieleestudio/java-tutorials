package com.example.ms.product.application.impl;

import com.example.ms.product.application.ProductAppService;
import com.example.ms.product.application.ProductService;
import com.example.ms.product.application.command.CreateProductCommand;
import com.example.ms.product.application.command.DeductStockCommand;
import com.example.ms.product.application.dto.ProductDto;
import com.example.ms.product.application.query.GetAllProductsQuery;
import com.example.ms.product.application.query.GetProductByIdQuery;
import com.example.ms.product.domain.Product;
import com.example.ms.product.domain.ProductRepository;
import com.example.ms.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link ProductAppService} (which extends the cross-context ProductService).
 */
@Service
public class ProductServiceImpl implements ProductAppService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public ProductDto createProduct(CreateProductCommand command) {
        Product product = Product.create(command.name(), command.price(), command.stock());
        productRepository.save(product);
        return toDto(product);
    }

    @Override
    @Transactional
    public void deductStock(DeductStockCommand command) {
        Product product = findProductOrThrow(command.productId());
        product.deductStock(command.quantity());
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductById(GetProductByIdQuery query) {
        Product product = findProductOrThrow(query.productId());
        return toDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts(GetAllProductsQuery query) {
        return productRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProduct(String productId) {
        Product product = findProductOrThrow(productId);
        return toDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getProducts(List<String> productIds) {
        return productRepository.findAllByIds(productIds).stream()
                .map(this::toDto)
                .toList();
    }

    private Product findProductOrThrow(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("PRODUCT_NOT_FOUND",
                        "Product not found: " + productId));
    }

    private ProductDto toDto(Product product) {
        return new ProductDto(product.getId(), product.getName(),
                product.getPrice(), product.getStock());
    }
}