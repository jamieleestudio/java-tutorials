package com.example.architecture.evolvable.order.domain;

import java.util.Optional;

/**
 * Domain Repository interface for Order.
 * Defers the implementation to the infrastructure layer.
 */
public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(String id);
}
