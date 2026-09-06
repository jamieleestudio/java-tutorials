package com.example.eda.order.interfaces.web;

import java.math.BigDecimal;

public record OrderLineResponse(String productId, String productName,
                                int quantity, BigDecimal unitPrice) {
}