package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 使用 AgentScope Java HarnessAgent 实现的聊天 Agent。
 *
 * <p>{@link HarnessAgent} 在 ReActAgent 之上提供 workspace、分层记忆、
 * 子 Agent 编排、中间件等工程化能力。
 *
 * <p>模型采用 DeepSeek（OpenAI 兼容接口），通过 {@link OpenAIChatModel}
 * 直接构建：apiKey 从环境变量 {@code OPENAI_API_KEY} 读取，
 * baseUrl 指向 {@code https://api.deepseek.com}。
 *
 * <p>Agent 实例通过 lazy 持有者构造，避免在无 API Key 的单元测试中
 * 过早触发模型解析。
 */
@Component
public class ChatAgent {

    private static final String DEEPSEEK_BASE_URL = "https://api.deepseek.com";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public ChatAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:" + DEEPSEEK_BASE_URL + "}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    /**
     * 以阻塞方式调用 Agent，返回最终文本回复。
     */
    public String chat(String message) {
        return agent().call(new UserMessage(message), runtimeContext()).block().getTextContent();
    }

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey)
                            .modelName(modelName)
                            .baseUrl(baseUrl)
                            .build();
                    local = HarnessAgent.builder()
                            .name("assistant")
                            .sysPrompt("你是一个乐于助人的中文智能助手，请简洁、准确地回答用户的问题。")
                            .model(model)
                            .workspace(Paths.get(".agentscope/workspace"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("demo").userId("alice").build();
    }
}