package com.example.eda.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Product service (event-driven) — port 8083.
 * Consumes order-events (deduct/restock), publishes product-events via outbox,
 * exposes sync catalog read for order-service.
 */
@SpringBootApplication
@EnableScheduling
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}