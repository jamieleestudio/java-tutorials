package com.example.cn.product.application.command;

/**
 * Command to deduct product stock (write operation).
 */
public record DeductStockCommand(String productId, int quantity) {
}