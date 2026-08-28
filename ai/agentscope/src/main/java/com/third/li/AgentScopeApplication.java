package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * AgentScope Java 聊天示例入口。
 *
 * 通过 agentscope-harness + agentscope-extensions-model-dashscope 构建 HarnessAgent，
 * 由 ChatController 暴露 HTTP 聊天接口。
 */
@SpringBootApplication
public class AgentScopeApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(AgentScopeApplication.class).run(args);
    }
}