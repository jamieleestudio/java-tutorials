package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 可观测性（Micrometer + 追踪 + SimpleLoggerAdvisor）。
 *
 * <p>Spring AI 2.0 内置 Micrometer 观测（指标 + 追踪）：
 * <ul>
 *   <li><b>指标</b>：每次调用自动记录 token 数、延迟等（actuator /metrics）</li>
 *   <li><b>追踪</b>：配合 Micrometer Tracing 形成调用链</li>
 *   <li>{@link SimpleLoggerAdvisor}：请求/响应日志（最轻量的观测）</li>
 * </ul>
 *
 * <p>启用 actuator：加 <code>spring-boot-starter-actuator</code> 依赖后
 * 访问 <code>/actuator/metrics</code> 查看指标。
 */
@RestController
public class ObservabilityController {

    private final ChatClient chatClient;

    public ObservabilityController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    /** 带日志观测的调用。 */
    @GetMapping("/ai/observability")
    public String chat(
            @RequestParam(value = "message", defaultValue = "介绍一下可观测性三支柱") String message) {
        return chatClient.prompt(message).call().content();
    }

    /** 可观测性说明。 */
    @GetMapping("/ai/observability/info")
    public String info() {
        return """
                可观测性三支柱：
                1. Metrics（指标）：token 用量、延迟、错误率
                   - 端点：/actuator/metrics
                   - 相关：spring.ai.* observation
                2. Tracing（追踪）：跨服务调用链
                   - 依赖：spring-boot-starter-actuator + Micrometer Tracing
                3. Logging（日志）：SimpleLoggerAdvisor 打印请求/响应

                开启 actuator 后访问 /actuator/metrics 查看 AI 调用指标。
                """;
    }
}
