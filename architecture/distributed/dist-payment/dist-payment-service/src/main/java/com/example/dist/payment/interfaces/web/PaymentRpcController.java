package com.example.dist.payment.interfaces.web;

import com.example.dist.payment.application.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Internal RPC contract exposed by the payment service for the order service.
 * Maps the provider-defined PaymentService interface onto HTTP endpoints.
 */
@RestController
@RequestMapping("/rpc/payments")
public class PaymentRpcController {

    private final PaymentService paymentService;

    public PaymentRpcController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Idempotent: paying the same order twice returns the same payment ID.
     */
    @PostMapping("/pay")
    public String pay(@RequestParam String orderId, @RequestParam BigDecimal amount) {
        return paymentService.pay(orderId, amount);
    }

    @PostMapping("/{paymentId}/refund")
    public void refund(@PathVariable String paymentId) {
        paymentService.refund(paymentId);
    }
}