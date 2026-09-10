package com.example.ms.order.interfaces.web;

import com.example.ms.order.application.dto.OrderDto;
import com.example.ms.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * Web response DTO for order — interfaces layer only.
 */
public record OrderResponse(
        String orderId,
        String customerId,
        OrderStatus status,
        BigDecimal totalAmount,
        List<OrderLineResponse> lines
) {
    public static OrderResponse from(OrderDto dto) {
        List<OrderLineResponse> lines = dto.lines().stream()
                .map(line -> new OrderLineResponse(line.productId(), line.productName(),
                        line.quantity(), line.unitPrice()))
                .toList();
        return new OrderResponse(dto.orderId(), dto.customerId(),
                dto.status(), dto.totalAmount(), lines);
    }
}