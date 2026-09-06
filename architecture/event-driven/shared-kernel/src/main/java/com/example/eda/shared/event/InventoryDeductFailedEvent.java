package com.example.eda.shared.event;

/**
 * Published by product-service when inventory deduction fails (e.g. out of stock).
 * Triggers order cancellation (saga compensation).
 */
public record InventoryDeductFailedEvent(
        String orderId,
        String reason
) implements IntegrationEvent {

    @Override
    public String topic() {
        return "product-events";
    }

    @Override
    public String aggregateId() {
        return orderId;
    }
}