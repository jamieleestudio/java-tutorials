package com.example.eda.payment.application;

import java.math.BigDecimal;

/**
 * Payment side of the saga: react to order events, publish payment events.
 */
public interface PaymentSagaService {

    /**
     * OrderCreatedEvent → pay → publish PaymentSucceededEvent.
     * Idempotent by orderId.
     */
    void processOrderPayment(String eventId, String orderId, BigDecimal amount);

    /**
     * OrderCancelledEvent → refund (saga compensation).
     */
    void refundOrderPayment(String eventId, String orderId);
}