package com.example.eda.order.application.command;

import com.example.eda.shared.event.IntegrationEvent;

/**
 * Contract for publishing INTEGRATION events via the local message table (outbox).
 * Implemented by infrastructure — guarantees business data + event are written
 * in the SAME local transaction (no lost messages).
 */
public interface EventPublisherService {

    void publish(IntegrationEvent event);
}