package com.example.erp.platform.persistence;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "com.example.erp")
@EnableJpaRepositories(basePackages = "com.example.erp")
public class ErpJpaConfiguration {
}
