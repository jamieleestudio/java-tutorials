package com.example.ms.order.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository port for the Order aggregate.
 * This is a domain-defined interface — infrastructure layer provides the implementation.
 */
public interface OrderRepository {

    void save(Order order);

    Optional<Order> findById(String orderId);

    List<Order> findByCustomerId(String customerId);
}