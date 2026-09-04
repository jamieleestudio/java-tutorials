package com.example.monolithic.order.domain.event;

import com.example.monolithic.shared.DomainEvent;

/**
 * Domain event raised when an order is paid.
 */
public record OrderPaidEvent(
        String orderId,
        String paymentId,
        long timestamp
) implements DomainEvent {

    @Override
    public String getAggregateId() {
        return orderId;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}