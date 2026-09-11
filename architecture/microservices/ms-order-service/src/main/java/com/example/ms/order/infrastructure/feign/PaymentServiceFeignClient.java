package com.example.ms.order.infrastructure.feign;

import com.example.ms.payment.application.PaymentService;
import org.springframework.stereotype.Component;

/**
 * ③→⑤ evolution: PaymentService implementation swapped from RestClient RPC client
 * to Feign client (Nacos discovery + LoadBalancer + Resilience4j).
 * OrderServiceImpl stays unchanged — still injects PaymentService.
 */
@Component
public class PaymentServiceFeignClient implements PaymentService {

    private final PaymentClient paymentClient;

    public PaymentServiceFeignClient(PaymentClient paymentClient) {
        this.paymentClient = paymentClient;
    }

    @Override
    public String pay(String orderId, java.math.BigDecimal amount) {
        return paymentClient.pay(orderId, amount);
    }

    @Override
    public void refund(String paymentId) {
        paymentClient.refund(paymentId);
    }
}