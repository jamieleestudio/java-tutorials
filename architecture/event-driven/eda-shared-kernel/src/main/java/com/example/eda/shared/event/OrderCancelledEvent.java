package com.example.eda.shared.event;

/**
 * Published by order-service when an order is cancelled.
 * Triggers compensation: payment refund + inventory restock.
 */
public record OrderCancelledEvent(
        String orderId,
        String reason
) implements IntegrationEvent {

    @Override
    public String topic() {
        return "order-events";
    }

    @Override
    public String aggregateId() {
        return orderId;
    }
}