package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-orchestrator-workers 示例入口。 */
@SpringBootApplication
public class OrchestratorWorkersApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(OrchestratorWorkersApplication.class).run(args);
    }
}
