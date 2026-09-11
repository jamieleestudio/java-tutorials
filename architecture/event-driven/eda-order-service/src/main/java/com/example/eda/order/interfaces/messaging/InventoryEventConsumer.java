package com.example.eda.order.interfaces.messaging;

import com.example.eda.order.application.OrderService;
import com.example.eda.shared.event.EventEnvelope;
import com.example.eda.shared.event.InventoryDeductedEvent;
import com.example.eda.shared.event.InventoryDeductFailedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Saga reaction: inventory events → order confirms or cancels.
 */
@Component
public class InventoryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    public InventoryEventConsumer(OrderService orderService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "product-events", groupId = "order-service")
    public void onMessage(String payload) {
        try {
            EventEnvelope envelope = objectMapper.readValue(payload, EventEnvelope.class);
            switch (envelope.type()) {
                case "InventoryDeductedEvent" -> {
                    InventoryDeductedEvent event = objectMapper.treeToValue(envelope.payload(), InventoryDeductedEvent.class);
                    orderService.onInventoryDeducted(envelope.eventId(), event.orderId());
                }
                case "InventoryDeductFailedEvent" -> {
                    InventoryDeductFailedEvent event = objectMapper.treeToValue(envelope.payload(), InventoryDeductFailedEvent.class);
                    orderService.onInventoryDeductFailed(envelope.eventId(), event.orderId(), event.reason());
                }
                default -> log.debug("Ignored event type {}", envelope.type());
            }
        } catch (Exception e) {
            log.error("Failed to handle inventory event", e);
        }
    }
}