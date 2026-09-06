package com.example.ms.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Order microservice — port 8081.
 * ⑤ adds: Nacos discovery/config, Feign + LoadBalancer, Resilience4j, tracing.
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.ms.order.infrastructure.feign")
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}