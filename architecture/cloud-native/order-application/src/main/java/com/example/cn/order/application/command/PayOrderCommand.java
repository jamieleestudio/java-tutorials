package com.example.cn.order.application.command;

/**
 * Command to pay an order (write operation).
 */
public record PayOrderCommand(String orderId) {
}