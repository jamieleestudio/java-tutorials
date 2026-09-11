package com.example.eda.shared.event;

/**
 * Published by product-service after inventory deduction succeeds.
 */
public record InventoryDeductedEvent(
        String orderId
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