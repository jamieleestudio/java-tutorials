package com.example.eda.order.interfaces.web;

import com.example.eda.order.application.dto.OrderDto;
import com.example.eda.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        String orderId,
        String customerId,
        OrderStatus status,
        BigDecimal totalAmount,
        boolean paymentSucceeded,
        boolean inventoryDeducted,
        List<OrderLineResponse> lines
) {
    public static OrderResponse from(OrderDto dto) {
        List<OrderLineResponse> lines = dto.lines().stream()
                .map(line -> new OrderLineResponse(line.productId(), line.productName(),
                        line.quantity(), line.unitPrice()))
                .toList();
        return new OrderResponse(dto.orderId(), dto.customerId(), dto.status(), dto.totalAmount(),
                dto.paymentSucceeded(), dto.inventoryDeducted(), lines);
    }
}