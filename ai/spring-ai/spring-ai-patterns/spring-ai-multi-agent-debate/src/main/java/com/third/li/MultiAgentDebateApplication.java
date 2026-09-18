package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** spring-ai-multi-agent-debate 示例入口。 */
@SpringBootApplication
public class MultiAgentDebateApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(MultiAgentDebateApplication.class).run(args);
    }
}
