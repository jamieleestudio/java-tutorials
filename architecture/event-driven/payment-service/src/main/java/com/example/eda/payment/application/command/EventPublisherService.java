package com.example.eda.payment.application.command;

import com.example.eda.shared.event.IntegrationEvent;

/**
 * Outbox publisher contract (payment side of the saga).
 */
public interface EventPublisherService {

    void publish(IntegrationEvent event);
}