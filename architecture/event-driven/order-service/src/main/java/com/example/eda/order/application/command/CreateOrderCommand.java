package com.example.eda.order.application.command;

import java.util.List;

/**
 * Command to create an order — starts the saga (payment + inventory run async).
 */
public record CreateOrderCommand(
        String customerId,
        List<OrderLine> lines
) {
    public record OrderLine(String productId, int quantity) {
    }
}