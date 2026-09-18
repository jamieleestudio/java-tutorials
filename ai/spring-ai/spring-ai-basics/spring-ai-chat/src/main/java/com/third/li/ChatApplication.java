package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** spring-ai-chat 示例入口。 */
@SpringBootApplication
public class ChatApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ChatApplication.class).run(args);
    }
}
