package com.example.architecture.evolvable.order.api;

import java.math.BigDecimal;

public record OrderDTO(
    String id,
    String productName,
    BigDecimal amount,
    String status
) {}
