package com.example.erp.platform.messaging;

import java.time.LocalDateTime;

public record MessageEnvelope(String topic, String payload, LocalDateTime occurredAt) {

    public static MessageEnvelope of(String topic, String payload) {
        return new MessageEnvelope(topic, payload, LocalDateTime.now());
    }
}