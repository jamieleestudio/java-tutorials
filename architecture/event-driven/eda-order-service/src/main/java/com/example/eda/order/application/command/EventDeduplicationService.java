package com.example.eda.order.application.command;

/**
 * Consumer-side idempotency contract: at-least-once delivery means
 * duplicate events MUST be recognized and skipped.
 */
public interface EventDeduplicationService {

    boolean isDuplicate(String eventId);

    void markProcessed(String eventId);
}