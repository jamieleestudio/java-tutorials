package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * AgentInstrumentation（可观测性接入点）示例入口。
 */
@SpringBootApplication
public class OtelApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(OtelApplication.class).run(args);
    }
}
