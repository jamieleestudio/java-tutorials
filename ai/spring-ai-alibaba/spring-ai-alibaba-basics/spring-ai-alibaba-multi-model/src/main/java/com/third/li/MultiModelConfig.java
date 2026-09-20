package com.third.li;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.setup.OpenAiSetup;

import com.openai.client.OpenAIClient;

import java.util.List;
import java.util.Map;

/**
 * 多模型工厂：手动构建第二个 ChatModel（通义千问，DashScope OpenAI 兼容模式）。
 *
 * <p>注意：这里<b>刻意不把 Qwen 模型注册成 Spring Bean</b> ——
 * Spring AI 的 OpenAI 自动配置带 {@code @ConditionalOnMissingBean(ChatModel.class)}，
 * 一旦用户自定义了 ChatModel Bean，默认的 openAiChatModel（DeepSeek）就不会再创建。
 * 所以用工厂方法按需构建，默认模型交给自动配置。
 *
 * <p>Spring AI 2.0 的 OpenAI 集成基于 OpenAI 官方 Java SDK（{@code com.openai:openai-java}）。
 * 用 {@link OpenAiSetup#setupSyncClient} 构建 {@link OpenAIClient}，
 * 再用 {@link OpenAiChatModel#builder()} 装配成第二个 {@code ChatModel}。
 * DashScope 原生 starter（spring-ai-alibaba-starter-dashscope）做的是同一件事：
 * 把通义千问封装成 {@code ChatModel}，此处用兼容模式演示同一抽象。
 */
final class MultiModelConfig {

    /** DashScope OpenAI 兼容模式端点。 */
    static final String DASHSCOPE_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    private MultiModelConfig() {
    }

    /** 构建通义千问（qwen-plus）ChatModel；空 apiKey 时可构建但调用会 401。 */
    static OpenAiChatModel buildQwenModel(String apiKey) {
        OpenAIClient client = OpenAiSetup.setupSyncClient(
                DASHSCOPE_BASE_URL,
                apiKey,
                null, null, null, null,
                false, false,
                null, java.time.Duration.ofSeconds(60), 2, null,
                Map.of(),
                ObservationRegistry.NOOP, null, List.of());
        return OpenAiChatModel.builder()
                .openAiClient(client)
                .options(OpenAiChatOptions.builder()
                        .model("qwen-plus")
                        .temperature(0.7)
                        .build())
                .build();
    }
}
