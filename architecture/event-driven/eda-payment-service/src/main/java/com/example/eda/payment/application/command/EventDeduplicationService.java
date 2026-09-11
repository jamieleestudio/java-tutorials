package com.example.eda.payment.application.command;

/**
 * Consumer-side idempotency contract.
 */
public interface EventDeduplicationService {

    boolean isDuplicate(String eventId);

    void markProcessed(String eventId);
}