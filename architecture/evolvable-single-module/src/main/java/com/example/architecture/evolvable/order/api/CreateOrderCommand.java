package com.example.architecture.evolvable.order.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateOrderCommand(
    @NotBlank(message = "Product name is required")
    String productName,
    
    @Positive(message = "Amount must be positive")
    BigDecimal amount
) {}
