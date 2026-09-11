package com.example.eda.payment.interfaces.messaging;

import com.example.eda.payment.application.PaymentSagaService;
import com.example.eda.shared.event.EventEnvelope;
import com.example.eda.shared.event.OrderCancelledEvent;
import com.example.eda.shared.event.OrderCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Payment saga reaction: order events → pay / refund.
 */
@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final PaymentSagaService paymentSagaService;
    private final ObjectMapper objectMapper;

    public OrderEventConsumer(PaymentSagaService paymentSagaService, ObjectMapper objectMapper) {
        this.paymentSagaService = paymentSagaService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order-events", groupId = "payment-service")
    public void onMessage(String payload) {
        try {
            EventEnvelope envelope = objectMapper.readValue(payload, EventEnvelope.class);
            switch (envelope.type()) {
                case "OrderCreatedEvent" -> {
                    OrderCreatedEvent event = objectMapper.treeToValue(envelope.payload(), OrderCreatedEvent.class);
                    paymentSagaService.processOrderPayment(envelope.eventId(), event.orderId(), event.totalAmount());
                }
                case "OrderCancelledEvent" -> {
                    OrderCancelledEvent event = objectMapper.treeToValue(envelope.payload(), OrderCancelledEvent.class);
                    paymentSagaService.refundOrderPayment(envelope.eventId(), event.orderId());
                }
                default -> log.debug("Ignored event type {}", envelope.type());
            }
        } catch (Exception e) {
            log.error("Failed to handle order event", e);
        }
    }
}