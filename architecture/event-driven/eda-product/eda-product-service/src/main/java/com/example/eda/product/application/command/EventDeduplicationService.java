package com.example.eda.product.application.command;

public interface EventDeduplicationService {

    boolean isDuplicate(String eventId);

    void markProcessed(String eventId);
}