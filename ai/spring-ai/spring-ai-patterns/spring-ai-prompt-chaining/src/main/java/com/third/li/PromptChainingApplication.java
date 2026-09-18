package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** spring-ai-prompt-chaining 示例入口。 */
@SpringBootApplication
public class PromptChainingApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(PromptChainingApplication.class).run(args);
    }
}
