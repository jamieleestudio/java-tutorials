package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** spring-ai-chat-memory-advisor 示例入口。 */
@SpringBootApplication
public class ChatMemoryAdvisorApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ChatMemoryAdvisorApplication.class).run(args);
    }
}
