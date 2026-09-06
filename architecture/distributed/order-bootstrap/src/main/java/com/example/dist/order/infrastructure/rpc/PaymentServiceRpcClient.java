package com.example.dist.order.infrastructure.rpc;

import com.example.dist.payment.application.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.function.Supplier;

/**
 * ②→③ evolution point: PaymentService implementation swapped from
 * in-process adapter to RPC client (RestClient + timeout + retry).
 * OrderServiceImpl stays unchanged — it still injects PaymentService.
 */
@Component
public class PaymentServiceRpcClient implements PaymentService {

    private static final int MAX_ATTEMPTS = 2;
    private static final int CONNECT_TIMEOUT_MS = 2000;
    private static final int READ_TIMEOUT_MS = 3000;

    private final RestClient restClient;

    public PaymentServiceRpcClient(@Value("${payment.service.base-url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(READ_TIMEOUT_MS);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    @Override
    public String pay(String orderId, BigDecimal amount) {
        return withRetry(() -> restClient.post()
                .uri(uri -> uri.path("/rpc/payments/pay")
                        .queryParam("orderId", orderId)
                        .queryParam("amount", amount)
                        .build())
                .retrieve()
                .body(String.class));
    }

    @Override
    public void refund(String paymentId) {
        withRetry(() -> restClient.post()
                .uri("/rpc/payments/{paymentId}/refund", paymentId)
                .retrieve()
                .toBodilessEntity());
    }

    /**
     * Simple retry for transient network failures (idempotency is guaranteed on the provider side).
     */
    private <T> T withRetry(Supplier<T> call) {
        ResourceAccessException last = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return call.get();
            } catch (ResourceAccessException e) {
                last = e;
            }
        }
        throw last;
    }
}