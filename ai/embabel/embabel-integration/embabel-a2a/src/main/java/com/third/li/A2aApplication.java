package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * A2A（Agent2Agent）示例入口。
 *
 * <p>引入 {@code embabel-agent-starter-a2a} 后，应用会自动暴露 A2A 服务端：
 * <ul>
 *   <li>{@code GET /.well-known/agent.json} —— Agent Card（技能/能力描述）</li>
 *   <li>JSON-RPC 端点 —— 接收 {@code message/send} 等 A2A 方法</li>
 * </ul>
 * 平台上的目标会被转换成 A2A 的 skill。
 */
@SpringBootApplication
public class A2aApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(A2aApplication.class).run(args);
    }
}
