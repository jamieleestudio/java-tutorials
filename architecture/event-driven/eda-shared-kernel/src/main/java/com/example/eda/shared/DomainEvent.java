package com.example.eda.shared;

/**
 * Marker interface for domain events.
 * Domain events represent something meaningful that happened in the domain.
 * They are pure POJOs with no framework dependencies.
 */
public interface DomainEvent {

    /**
     * The unique identifier of the aggregate that raised this event.
     */
    String getAggregateId();

    /**
     * When the event occurred (epoch millis).
     */
    long getTimestamp();
}