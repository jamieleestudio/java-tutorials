package com.example.monolithic.order.domain;

import com.example.monolithic.shared.BusinessRuleViolationException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Order item value object.
 * Represents a product line within an order.
 * Pure POJO, no framework dependencies.
 */
public class OrderItem {

    private final String productId;
    private final String productName;
    private final int quantity;
    private final BigDecimal unitPrice;

    public OrderItem(String productId, String productName, int quantity, BigDecimal unitPrice) {
        if (productId == null || productId.isBlank()) {
            throw new BusinessRuleViolationException("INVALID_ITEM", "Product ID must not be empty");
        }
        if (quantity <= 0) {
            throw new BusinessRuleViolationException("INVALID_ITEM", "Quantity must be positive");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleViolationException("INVALID_ITEM", "Unit price must not be negative");
        }
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public BigDecimal subTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(productId, orderItem.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }
}