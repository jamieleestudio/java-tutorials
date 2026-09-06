package com.example.eda.product.infrastructure.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Polls the outbox table and relays unpublished messages to Kafka.
 * At-least-once delivery: consumers deduplicate by eventId.
 */
@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxJpaRepository outboxJpaRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxRelay(OutboxJpaRepository outboxJpaRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxJpaRepository = outboxJpaRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void relay() {
        List<OutboxMessageEntity> pending = outboxJpaRepository
                .findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
        for (OutboxMessageEntity message : pending) {
            kafkaTemplate.send(message.getTopic(), message.getAggregateId(), message.getPayload());
            message.setPublishedAt(LocalDateTime.now());
            log.debug("Relayed outbox message eventId={} topic={}", message.getEventId(), message.getTopic());
        }
    }
}