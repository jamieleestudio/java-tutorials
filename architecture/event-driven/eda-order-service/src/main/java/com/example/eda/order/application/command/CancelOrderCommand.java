package com.example.eda.order.application.command;

/**
 * Command to cancel an order — triggers saga compensation
 * (payment refund + inventory restock via OrderCancelledEvent).
 */
public record CancelOrderCommand(String orderId, String reason) {
}