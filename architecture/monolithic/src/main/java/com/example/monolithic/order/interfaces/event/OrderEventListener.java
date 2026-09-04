package com.example.monolithic.order.interfaces.event;

import com.example.monolithic.order.domain.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event listener for order domain events.
 * Lives in interfaces layer — handles event consumption.
 */
@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Order created: orderId={}, productIds={}, total={}",
                event.orderId(), event.productIds(), event.totalAmount());
    }
}