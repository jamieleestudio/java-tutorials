package com.example.dist.order.application.command;

import java.util.List;

/**
 * Command to create a new order (write operation).
 */
public record CreateOrderCommand(
        String customerId,
        List<OrderLine> lines
) {
    public record OrderLine(String productId, int quantity) {
    }
}