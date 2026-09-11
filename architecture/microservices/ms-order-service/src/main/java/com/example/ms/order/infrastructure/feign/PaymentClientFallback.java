package com.example.ms.order.infrastructure.feign;

import com.example.ms.shared.BusinessRuleViolationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Circuit-breaker fallback: payment service unreachable / failing fast.
 * Surfaces a domain-style error instead of propagating transport exceptions.
 */
@Component
public class PaymentClientFallback implements PaymentClient {

    @Override
    public String pay(String orderId, BigDecimal amount) {
        throw new BusinessRuleViolationException("PAYMENT_SERVICE_UNAVAILABLE",
                "Payment service is currently unavailable, please retry later");
    }

    @Override
    public void refund(String paymentId) {
        throw new BusinessRuleViolationException("PAYMENT_SERVICE_UNAVAILABLE",
                "Payment service is currently unavailable, please retry later");
    }
}