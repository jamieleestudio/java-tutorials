package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 可观测（Micrometer 指标 + SimpleLoggerAdvisor + Actuator）。
 *
 * <p>Spring AI 的观测能力在 SAA 里原样可用，而且 SAA 的 Graph / Agent
 * 都挂了 ObservationRegistry（Builder.observationRegistry），粒度更细：
 * <ul>
 *   <li>指标 — 每次 ChatModel / ChatClient 调用自动上报 token 用量与延迟，
 *       启用 actuator 后访问 /actuator/metrics 查看</li>
 *   <li>日志 — {@link SimpleLoggerAdvisor} 打印每次请求/响应（最轻量观测）</li>
 *   <li>平台 — 生产可接 SAA Studio / 阿里云 ARMS / OTel Collector</li>
 * </ul>
 */
@RestController
public class ObservabilityController {

    private final ChatClient chatClient;
    private final ChatModel chatModel;

    public ObservabilityController(ChatClient.Builder chatClientBuilder, ChatModel chatModel) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
        this.chatModel = chatModel;
    }

    /** 带观测的调用：日志 + 指标自动产生。 */
    @GetMapping("/observability/run")
    public String run(
            @RequestParam(value = "message", defaultValue = "一句话介绍 Spring AI Alibaba 的可观测能力")
            String message) {
        return chatClient.prompt(message).call().content();
    }

    /** 查看当前可用的观测指标名（过滤 AI 相关）。 */
    @GetMapping("/observability/metrics")
    public String metrics() {
        return """
                启动后先调用 /observability/run 触发一次 AI 调用，然后访问：
                  /actuator/metrics                          — 全部指标
                  /actuator/metrics/gen.ai.client.token.usage — token 用量（Spring AI 上报）
                  /actuator/metrics/gen.ai.client.operation   — AI 调用延迟
                Graph / Agent 的观测：builder().observationRegistry(registry) 已默认挂载 NOOP，
                引入 micrometer-registry 后换成真实 registry 即可上报。
                """;
    }
}
