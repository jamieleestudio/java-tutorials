package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-subagent-handoff 示例入口。 */
@SpringBootApplication
public class SubagentHandoffApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(SubagentHandoffApplication.class).run(args);
    }
}
