package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-chatui 示例入口。 */
@SpringBootApplication
public class ChatuiApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ChatuiApplication.class).run(args);
    }
}