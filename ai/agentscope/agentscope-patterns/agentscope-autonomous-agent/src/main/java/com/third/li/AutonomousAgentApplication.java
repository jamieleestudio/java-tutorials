package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-autonomous-agent 示例入口。 */
@SpringBootApplication
public class AutonomousAgentApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(AutonomousAgentApplication.class).run(args);
    }
}
