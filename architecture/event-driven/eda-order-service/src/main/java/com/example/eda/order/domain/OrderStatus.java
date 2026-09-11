package com.example.eda.order.domain;

/**
 * Order status in the saga world: payment and inventory run ASYNC.
 */
public enum OrderStatus {
    CREATED,
    CONFIRMED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case CREATED -> target == CANCELLED;
            case CONFIRMED, CANCELLED -> false;
        };
    }
}