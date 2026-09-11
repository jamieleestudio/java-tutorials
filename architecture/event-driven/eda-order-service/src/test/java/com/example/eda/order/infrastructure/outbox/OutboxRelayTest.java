package com.example.eda.order.infrastructure.outbox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxRelayTest {

    @Mock
    private OutboxJpaRepository outboxJpaRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void relay_publishes_pending_messages_and_marks_published() {
        OutboxMessageEntity message = new OutboxMessageEntity(
                "event-1", "order-events", "order-1", "OrderCreatedEvent", "{}");
        when(outboxJpaRepository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc())
                .thenReturn(List.of(message));

        OutboxRelay relay = new OutboxRelay(outboxJpaRepository, kafkaTemplate);
        relay.relay();

        verify(kafkaTemplate).send("order-events", "order-1", "{}");
        assertThat(message.getPublishedAt()).isNotNull();
    }

    @Test
    void relay_with_empty_outbox_does_nothing() {
        when(outboxJpaRepository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc())
                .thenReturn(List.of());

        OutboxRelay relay = new OutboxRelay(outboxJpaRepository, kafkaTemplate);
        relay.relay();

        verify(kafkaTemplate, org.mockito.Mockito.never())
                .send(org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString());
    }
}