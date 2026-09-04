package com.example.monolithic.product.infrastructure;

import com.example.monolithic.product.domain.Product;
import com.example.monolithic.product.domain.ProductRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    public ProductRepositoryImpl(ProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Product product) {
        ProductEntity entity = new ProductEntity(
                product.getId(), product.getName(), product.getPrice(), product.getStock()
        );
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Product> findById(String productId) {
        return jpaRepository.findById(productId).map(this::toDomain);
    }

    @Override
    public List<Product> findAllByIds(List<String> productIds) {
        return jpaRepository.findAllById(productIds).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private Product toDomain(ProductEntity entity) {
        return Product.reconstitute(entity.getId(), entity.getName(), entity.getPrice(), entity.getStock());
    }
}