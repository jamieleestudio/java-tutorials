package com.example.ms.order.application.command;

import com.example.ms.shared.DomainEvent;

/**
 * Contract for publishing domain events.
 * Defined by the application layer, implemented by infrastructure (dependency inversion).
 */
public interface EventPublisherService {

    void publish(DomainEvent event);
}