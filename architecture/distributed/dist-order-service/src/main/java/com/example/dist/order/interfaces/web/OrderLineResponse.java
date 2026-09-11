package com.example.dist.order.interfaces.web;

import java.math.BigDecimal;

/**
 * Web response DTO for order line — interfaces layer only.
 */
public record OrderLineResponse(String productId, String productName,
                                int quantity, BigDecimal unitPrice) {
}