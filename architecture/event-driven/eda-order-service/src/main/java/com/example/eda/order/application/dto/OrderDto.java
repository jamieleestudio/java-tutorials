package com.example.eda.order.application.dto;

import com.example.eda.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record OrderDto(
        String orderId,
        String customerId,
        OrderStatus status,
        BigDecimal totalAmount,
        boolean paymentSucceeded,
        boolean inventoryDeducted,
        List<OrderLineDto> lines
) {
    public record OrderLineDto(String productId, String productName, int quantity, BigDecimal unitPrice) {
    }
}