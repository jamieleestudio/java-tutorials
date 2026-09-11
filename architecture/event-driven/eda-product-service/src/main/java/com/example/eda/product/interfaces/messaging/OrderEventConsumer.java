package com.example.eda.product.interfaces.messaging;

import com.example.eda.product.application.ProductAppService;
import com.example.eda.shared.event.EventEnvelope;
import com.example.eda.shared.event.OrderCancelledEvent;
import com.example.eda.shared.event.OrderCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Product saga reaction: order events → deduct stock / restock (compensation).
 */
@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final ProductAppService productAppService;
    private final ObjectMapper objectMapper;

    public OrderEventConsumer(ProductAppService productAppService, ObjectMapper objectMapper) {
        this.productAppService = productAppService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order-events", groupId = "product-service")
    public void onMessage(String payload) {
        try {
            EventEnvelope envelope = objectMapper.readValue(payload, EventEnvelope.class);
            switch (envelope.type()) {
                case "OrderCreatedEvent" -> {
                    OrderCreatedEvent event = objectMapper.treeToValue(envelope.payload(), OrderCreatedEvent.class);
                    productAppService.deductForOrder(envelope.eventId(), event);
                }
                case "OrderCancelledEvent" -> {
                    OrderCancelledEvent event = objectMapper.treeToValue(envelope.payload(), OrderCancelledEvent.class);
                    productAppService.restockForOrder(envelope.eventId(), event.orderId());
                }
                default -> log.debug("Ignored event type {}", envelope.type());
            }
        } catch (Exception e) {
            log.error("Failed to handle order event", e);
        }
    }
}