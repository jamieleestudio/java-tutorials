package com.example.dist.order.application.dto;

import com.example.dist.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * Application-layer output contract for the Order context.
 * Consumed by the interfaces layer, converted to web Response there.
 */
public record OrderDto(
        String orderId,
        String customerId,
        OrderStatus status,
        BigDecimal totalAmount,
        List<OrderLineDto> lines
) {
    public record OrderLineDto(String productId, String productName, int quantity, BigDecimal unitPrice) {
    }
}