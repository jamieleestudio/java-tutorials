package com.example.mmm.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Monolithic multi-module bootstrap.
 * Assembles all bounded-context modules into ONE deployable application.
 */
@SpringBootApplication(scanBasePackages = "com.example.mmm")
@EntityScan(basePackages = "com.example.mmm")
@EnableJpaRepositories(basePackages = "com.example.mmm")
public class MonolithicMultiModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonolithicMultiModuleApplication.class, args);
    }
}