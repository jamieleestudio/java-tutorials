package com.example.monolithic.order.application.command;

import com.example.monolithic.shared.DomainEvent;

/**
 * Contract for publishing domain events.
 * Defined by the application layer, implemented by infrastructure (dependency inversion).
 */
public interface EventPublisherService {

    void publish(DomainEvent event);
}