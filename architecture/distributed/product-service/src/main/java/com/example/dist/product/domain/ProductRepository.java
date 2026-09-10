package com.example.dist.product.domain;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for the Product aggregate.
 */
public interface ProductRepository {

    void save(Product product);

    Optional<Product> findById(String productId);

    List<Product> findAllByIds(List<String> productIds);

    List<Product> findAll();
}