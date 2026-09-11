package com.example.eda.shared.event;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Envelope travelling on the event bus.
 * eventId drives consumer-side idempotency (at-least-once delivery).
 */
public record EventEnvelope(
        String eventId,
        String type,
        String aggregateId,
        String topic,
        JsonNode payload
) {
}