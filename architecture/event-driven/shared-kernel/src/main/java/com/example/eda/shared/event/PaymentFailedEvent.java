package com.example.eda.shared.event;

/**
 * Published by payment-service when payment fails.
 * Triggers order cancellation (saga compensation).
 */
public record PaymentFailedEvent(
        String orderId,
        String reason
) implements IntegrationEvent {

    @Override
    public String topic() {
        return "payment-events";
    }

    @Override
    public String aggregateId() {
        return orderId;
    }
}