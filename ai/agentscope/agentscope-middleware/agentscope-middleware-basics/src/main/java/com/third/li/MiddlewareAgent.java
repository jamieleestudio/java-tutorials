package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 带 LoggingMiddleware 的 Agent。
 *
 * <p>通过 {@code HarnessAgent.Builder.middleware(...)} 注入中间件。
 */
@Component
public class MiddlewareAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public MiddlewareAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public String chat(String message) {
        return agent().call(new UserMessage(message), runtimeContext()).block().getTextContent();
    }

    /** 不带中间件的 Agent（对照组）。 */
    public String chatWithoutMiddleware(String message) {
        return plainAgent().call(new UserMessage(message), runtimeContext()).block().getTextContent();
    }

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("middleware-agent")
                            .sysPrompt("你是一个乐于助人的中文智能助手。")
                            .model(model)
                            .middleware(new LoggingMiddleware())
                            .workspace(java.nio.file.Paths.get(".agentscope/workspace-mw"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private HarnessAgent plainAgent() {
        OpenAIChatModel model = OpenAIChatModel.builder()
                .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
        return HarnessAgent.builder()
                .name("plain-agent")
                .sysPrompt("你是一个乐于助人的中文智能助手。")
                .model(model)
                .workspace(java.nio.file.Paths.get(".agentscope/workspace-mw"))
                .build();
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("mw-demo").userId("alice").build();
    }
}