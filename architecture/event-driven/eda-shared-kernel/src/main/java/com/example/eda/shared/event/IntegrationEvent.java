package com.example.eda.shared.event;

/**
 * Marker contract for integration events published to the event bus (Kafka).
 * Domain events stay inside a context; integration events cross contexts.
 */
public interface IntegrationEvent {

    /**
     * Kafka topic this event is published to.
     */
    String topic();

    /**
     * Aggregate ID — used as Kafka message key (ordering per aggregate).
     */
    String aggregateId();
}