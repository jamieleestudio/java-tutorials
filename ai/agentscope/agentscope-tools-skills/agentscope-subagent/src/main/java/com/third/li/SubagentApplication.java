package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-subagent 示例入口。 */
@SpringBootApplication
public class SubagentApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(SubagentApplication.class).run(args);
    }
}