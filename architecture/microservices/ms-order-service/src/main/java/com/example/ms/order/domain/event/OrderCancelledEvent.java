package com.example.ms.order.domain.event;

import com.example.ms.shared.DomainEvent;

/**
 * Domain event raised when an order is cancelled.
 */
public record OrderCancelledEvent(
        String orderId,
        String reason,
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