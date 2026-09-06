package com.example.eda.shared.event;

/**
 * Published by payment-service after successful payment.
 */
public record PaymentSucceededEvent(
        String orderId,
        String paymentId
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