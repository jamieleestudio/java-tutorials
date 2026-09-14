package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * guardrails（护栏）示例入口。
 */
@SpringBootApplication
public class GuardrailsApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(GuardrailsApplication.class).run(args);
    }
}
