package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * AgentScope Java 聊天示例入口。
 *
 * <p>通过 agentscope-spring-boot-starter 自动配置 AgentScope 核心组件，
 * 通过 agentscope-openai-spring-boot-starter 自动注册 OpenAI 兼容模型（DeepSeek）。
 */
@SpringBootApplication
public class AgentScopeApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(AgentScopeApplication.class).run(args);
    }
}