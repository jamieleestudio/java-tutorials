package com.example.cn.order.application.query;

/**
 * Query to get orders by customer ID (read operation).
 */
public record GetOrdersByCustomerQuery(String customerId) {
}