package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 流式输出示例入口。
 *
 * <p>通过注入 {@code AiBuilder} 在 Agent/动作之外直接获取 {@code Ai} 网关，
 * 从而把模型输出以 SSE 形式逐块推送给前端。
 */
@SpringBootApplication
public class StreamingApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(StreamingApplication.class).run(args);
    }
}
