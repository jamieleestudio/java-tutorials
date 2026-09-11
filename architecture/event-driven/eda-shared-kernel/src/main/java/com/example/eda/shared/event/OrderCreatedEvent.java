package com.example.eda.shared.event;

import java.math.BigDecimal;
import java.util.List;

/**
 * Published by order-service when an order is created.
 * Starts the saga: payment + inventory deduction.
 */
public record OrderCreatedEvent(
        String orderId,
        String customerId,
        List<OrderLine> lines,
        BigDecimal totalAmount
) implements IntegrationEvent {

    @Override
    public String topic() {
        return "order-events";
    }

    @Override
    public String aggregateId() {
        return orderId;
    }

    public record OrderLine(String productId, String productName, int quantity, BigDecimal unitPrice) {
    }
}