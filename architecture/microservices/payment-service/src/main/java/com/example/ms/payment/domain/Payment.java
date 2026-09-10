package com.example.ms.payment.domain;

import com.example.ms.shared.AggregateRoot;
import com.example.ms.shared.BusinessRuleViolationException;
import com.example.ms.shared.IdGenerator;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Payment aggregate root.
 * Pure POJO, no framework dependencies.
 */
public class Payment extends AggregateRoot {

    private final String id;
    private final String orderId;
    private final BigDecimal amount;
    private PaymentStatus status;
    private String failureReason;

    private Payment(String id, String orderId, BigDecimal amount, PaymentStatus status) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
    }

    public static Payment create(String orderId, BigDecimal amount) {
        if (orderId == null || orderId.isBlank()) {
            throw new BusinessRuleViolationException("INVALID_PAYMENT", "Order ID must not be empty");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException("INVALID_PAYMENT", "Amount must be positive");
        }
        return new Payment(IdGenerator.newId(), orderId, amount, PaymentStatus.PENDING);
    }

    public static Payment reconstitute(String id, String orderId, BigDecimal amount,
                                        PaymentStatus status, String failureReason) {
        Payment payment = new Payment(id, orderId, amount, status);
        payment.failureReason = failureReason;
        return payment;
    }

    public void succeed() {
        if (status != PaymentStatus.PENDING) {
            throw new BusinessRuleViolationException("INVALID_TRANSITION",
                    "Cannot succeed payment in status " + status);
        }
        status = PaymentStatus.SUCCESS;
    }

    public void fail(String reason) {
        if (status != PaymentStatus.PENDING) {
            throw new BusinessRuleViolationException("INVALID_TRANSITION",
                    "Cannot fail payment in status " + status);
        }
        status = PaymentStatus.FAILED;
        failureReason = reason;
    }

    public void refund() {
        if (status != PaymentStatus.SUCCESS) {
            throw new BusinessRuleViolationException("INVALID_TRANSITION",
                    "Cannot refund payment in status " + status);
        }
        status = PaymentStatus.REFUNDED;
    }

    public String getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }
}