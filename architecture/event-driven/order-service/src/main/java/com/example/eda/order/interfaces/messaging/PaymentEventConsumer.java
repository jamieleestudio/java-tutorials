package com.example.eda.order.interfaces.messaging;

import com.example.eda.order.application.OrderService;
import com.example.eda.shared.event.EventEnvelope;
import com.example.eda.shared.event.PaymentFailedEvent;
import com.example.eda.shared.event.PaymentSucceededEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Saga reaction: payment events → order confirms or cancels.
 * Input adapter living in the interfaces layer; delegates to OrderService.
 */
@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    public PaymentEventConsumer(OrderService orderService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "payment-events", groupId = "order-service")
    public void onMessage(String payload) {
        try {
            EventEnvelope envelope = objectMapper.readValue(payload, EventEnvelope.class);
            switch (envelope.type()) {
                case "PaymentSucceededEvent" -> {
                    PaymentSucceededEvent event = objectMapper.treeToValue(envelope.payload(), PaymentSucceededEvent.class);
                    orderService.onPaymentSucceeded(envelope.eventId(), event.orderId(), event.paymentId());
                }
                case "PaymentFailedEvent" -> {
                    PaymentFailedEvent event = objectMapper.treeToValue(envelope.payload(), PaymentFailedEvent.class);
                    orderService.onPaymentFailed(envelope.eventId(), event.orderId(), event.reason());
                }
                default -> log.debug("Ignored event type {}", envelope.type());
            }
        } catch (Exception e) {
            log.error("Failed to handle payment event", e);
        }
    }
}