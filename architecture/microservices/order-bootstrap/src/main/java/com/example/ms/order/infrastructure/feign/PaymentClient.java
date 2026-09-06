package com.example.ms.order.infrastructure.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * Feign contract for the payment service RPC endpoints.
 * Service name resolves via Nacos; load-balanced client side.
 * Circuit breaker: falls back to {@link PaymentClientFallback} on failure.
 */
@FeignClient(name = "payment-service", path = "/rpc/payments", fallback = PaymentClientFallback.class)
public interface PaymentClient {

    @PostMapping("/pay")
    String pay(@RequestParam("orderId") String orderId, @RequestParam("amount") BigDecimal amount);

    @PostMapping("/{paymentId}/refund")
    void refund(@PathVariable("paymentId") String paymentId);
}