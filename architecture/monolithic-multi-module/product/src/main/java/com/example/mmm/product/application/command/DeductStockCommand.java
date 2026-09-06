package com.example.mmm.product.application.command;

/**
 * Command to deduct product stock (write operation).
 */
public record DeductStockCommand(String productId, int quantity) {
}