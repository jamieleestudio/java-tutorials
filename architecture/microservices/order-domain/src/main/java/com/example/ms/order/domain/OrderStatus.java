package com.example.ms.order.domain;

/**
 * Order status value object (enum).
 * Encapsulates the state machine of an order.
 */
public enum OrderStatus {
    CREATED,
    PAID,
    CONFIRMED,
    SHIPPED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case CREATED -> target == PAID || target == CANCELLED;
            case PAID -> target == CONFIRMED || target == CANCELLED;
            case CONFIRMED -> target == SHIPPED;
            case SHIPPED, CANCELLED -> false;
        };
    }
}