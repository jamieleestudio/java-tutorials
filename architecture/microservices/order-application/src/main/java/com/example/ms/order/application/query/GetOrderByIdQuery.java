package com.example.ms.order.application.query;

/**
 * Query to get an order by ID (read operation).
 */
public record GetOrderByIdQuery(String orderId) {
}