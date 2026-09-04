package com.example.monolithic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Monolithic application entry point.
 * Component scan covers all bounded contexts.
 */
@SpringBootApplication
public class MonolithicApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonolithicApplication.class, args);
    }
}