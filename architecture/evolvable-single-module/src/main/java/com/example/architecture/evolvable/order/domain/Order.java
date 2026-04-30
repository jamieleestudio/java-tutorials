package com.example.architecture.evolvable.order.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Domain Entity for Order.
 * Contains core business logic and state.
 * Does not depend on Spring, JPA, or web frameworks.
 */
public class Order {
    private final String id;
    private final String productName;
    private final BigDecimal amount;
    private String status;

    public Order(String id, String productName, BigDecimal amount, String status) {
        this.id = id;
        this.productName = productName;
        this.amount = amount;
        this.status = status;
    }

    public static Order create(String productName, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        return new Order(UUID.randomUUID().toString(), productName, amount, "CREATED");
    }

    public void pay() {
        if (!"CREATED".equals(this.status)) {
            throw new IllegalStateException("Order can only be paid if it is in CREATED status");
        }
        this.status = "PAID";
    }

    public String getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }
}
