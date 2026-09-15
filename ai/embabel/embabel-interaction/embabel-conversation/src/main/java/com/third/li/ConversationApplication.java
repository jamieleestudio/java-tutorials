package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 多轮对话示例入口。
 */
@SpringBootApplication
public class ConversationApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ConversationApplication.class).run(args);
    }
}
