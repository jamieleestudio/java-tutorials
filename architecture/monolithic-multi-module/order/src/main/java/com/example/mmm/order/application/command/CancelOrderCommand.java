package com.example.mmm.order.application.command;

/**
 * Command to cancel an order (write operation).
 */
public record CancelOrderCommand(String orderId, String reason) {
}