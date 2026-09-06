package com.example.mmm.product.domain;

import com.example.mmm.shared.BusinessRuleViolationException;
import com.example.mmm.shared.IdGenerator;

import java.math.BigDecimal;

/**
 * Product aggregate root.
 * Pure POJO, no framework dependencies.
 */
public class Product {

    private final String id;
    private final String name;
    private BigDecimal price;
    private int stock;

    private Product(String id, String name, BigDecimal price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public static Product create(String name, BigDecimal price, int stock) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException("INVALID_PRODUCT", "Product name must not be empty");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException("INVALID_PRODUCT", "Price must be positive");
        }
        if (stock < 0) {
            throw new BusinessRuleViolationException("INVALID_PRODUCT", "Stock must not be negative");
        }
        return new Product(IdGenerator.newId(), name, price, stock);
    }

    public static Product reconstitute(String id, String name, BigDecimal price, int stock) {
        return new Product(id, name, price, stock);
    }

    public void deductStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessRuleViolationException("INVALID_QUANTITY", "Quantity must be positive");
        }
        if (stock < quantity) {
            throw new BusinessRuleViolationException("OUT_OF_STOCK",
                    "Insufficient stock for product " + id + ": available=" + stock + ", requested=" + quantity);
        }
        stock -= quantity;
    }

    public void restock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessRuleViolationException("INVALID_QUANTITY", "Restock quantity must be positive");
        }
        stock += quantity;
    }

    public void changePrice(BigDecimal newPrice) {
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException("INVALID_PRICE", "Price must be positive");
        }
        price = newPrice;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }
}