package com.example.monolithic.order.domain.event;

import com.example.monolithic.order.domain.OrderStatus;
import com.example.monolithic.shared.DomainEvent;

import java.math.BigDecimal;
import java.util.List;

/**
 * Domain event raised when an order is created.
 */
public record OrderCreatedEvent(
        String orderId,
        List<String> productIds,
        BigDecimal totalAmount,
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

    public OrderStatus status() {
        return OrderStatus.CREATED;
    }
}