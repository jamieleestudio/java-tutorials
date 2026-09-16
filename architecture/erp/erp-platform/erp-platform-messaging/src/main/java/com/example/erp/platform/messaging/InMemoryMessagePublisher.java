package com.example.erp.platform.messaging;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InMemoryMessagePublisher implements MessagePublisher {

    private final List<MessageConsumer> consumers;

    public InMemoryMessagePublisher(List<MessageConsumer> consumers) {
        this.consumers = consumers;
    }

    @Override
    public void publish(MessageEnvelope envelope) {
        for (MessageConsumer consumer : consumers) {
            if (consumer.topic().equals(envelope.topic())) {
                consumer.onMessage(envelope);
            }
        }
    }
}