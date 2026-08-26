package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Embabel Agent Framework 聊天示例入口。
 *
 * 通过 embabel-agent-starter-openai 自动配置 Spring AI 的 ChatModel，
 * 由 ChatController 暴露 HTTP 聊天接口。
 */
@SpringBootApplication
public class EmbabelApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(EmbabelApplication.class).run(args);
    }
}
