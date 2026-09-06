package com.example.eda.product.infrastructure.outbox;

import com.example.eda.product.application.command.EventPublisherService;
import com.example.eda.shared.event.EventEnvelope;
import com.example.eda.shared.event.IntegrationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * Local message table implementation of {@link EventPublisherService}:
 * business data and the event are saved in the SAME local transaction.
 * The actual Kafka publish happens later via {@link OutboxRelay}.
 */
@Component
public class OutboxEventPublisher implements EventPublisherService {

    private final OutboxJpaRepository outboxJpaRepository;
    private final ObjectMapper objectMapper;

    public OutboxEventPublisher(OutboxJpaRepository outboxJpaRepository, ObjectMapper objectMapper) {
        this.outboxJpaRepository = outboxJpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(IntegrationEvent event) {
        try {
            String eventId = java.util.UUID.randomUUID().toString();
            EventEnvelope envelope = new EventEnvelope(
                    eventId,
                    event.getClass().getSimpleName(),
                    event.aggregateId(),
                    event.topic(),
                    objectMapper.valueToTree(event)
            );
            outboxJpaRepository.save(new OutboxMessageEntity(
                    eventId,
                    event.topic(),
                    event.aggregateId(),
                    event.getClass().getSimpleName(),
                    objectMapper.writeValueAsString(envelope)
            ));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize integration event", e);
        }
    }
}