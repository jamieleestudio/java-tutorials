package com.example.mmm.order.application.command;

import com.example.mmm.shared.DomainEvent;

/**
 * Contract for publishing domain events.
 * Defined by the application layer, implemented by infrastructure (dependency inversion).
 */
public interface EventPublisherService {

    void publish(DomainEvent event);
}