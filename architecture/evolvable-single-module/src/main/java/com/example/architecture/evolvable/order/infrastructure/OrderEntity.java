package com.example.architecture.evolvable.order.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    private String id;
    private String productName;
    private BigDecimal amount;
    private String status;

    protected OrderEntity() {
        // JPA requires no-arg constructor
    }

    public OrderEntity(String id, String productName, BigDecimal amount, String status) {
        this.id = id;
        this.productName = productName;
        this.amount = amount;
        this.status = status;
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
