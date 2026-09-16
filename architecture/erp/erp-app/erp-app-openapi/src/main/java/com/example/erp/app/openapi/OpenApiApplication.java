package com.example.erp.app.openapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(
        basePackages = "com.example.erp",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.example\\.erp\\..*\\.interfaces\\.(web|admin|mobile|internal|mq|job)\\..*"))
public class OpenApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenApiApplication.class, args);
    }
}