package com.example.dist.payment.application;

import java.math.BigDecimal;

/**
 * Payment provider contract — CROSS-CONTEXT methods only.
 * Consumed remotely by the order service (implemented there by an RPC client,
 * implemented here by PaymentServiceImpl).
 */
public interface PaymentService {

    /**
     * Pay for an order. Idempotent: same orderId returns the same payment ID.
     *
     * @return payment ID
     */
    String pay(String orderId, BigDecimal amount);

    /**
     * Refund a payment.
     */
    void refund(String paymentId);
}