package com.example.erp.app.mobile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(
        basePackages = "com.example.erp",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.example\\.erp\\..*\\.interfaces\\.(web|admin|openapi|internal|device|mq|job)\\..*"))
public class MobileApplication {

    public static void main(String[] args) {
        SpringApplication.run(MobileApplication.class, args);
    }
}