package com.example.ms.order.interfaces.web;

/**
 * Web request DTO for cancelling an order.
 * Order ID comes from the path; only additional parameters live in the body.
 */
public record CancelOrderRequest(String reason) {
}