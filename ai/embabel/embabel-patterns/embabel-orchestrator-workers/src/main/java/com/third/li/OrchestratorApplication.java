package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Orchestrator-workers 示例入口。
 */
@SpringBootApplication
public class OrchestratorApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(OrchestratorApplication.class).run(args);
    }
}
