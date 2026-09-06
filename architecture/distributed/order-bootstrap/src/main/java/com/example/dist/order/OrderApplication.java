package com.example.dist.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Order service — runs in its OWN JVM (port 8081).
 * Cross-context calls go through RPC clients in infrastructure,
 * OrderServiceImpl and domain code are IDENTICAL to ②.
 */
@SpringBootApplication
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}