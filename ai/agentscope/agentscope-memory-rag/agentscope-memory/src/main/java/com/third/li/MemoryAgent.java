package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.memory.MemoryConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 内存记忆 Agent：演示 AgentScope 的对话记忆配置。
 *
 * <p>AgentScope 的记忆层次：
 * <ul>
 *   <li>{@link io.agentscope.core.memory.InMemoryMemory} —— 默认短期记忆，存当前对话消息列表</li>
 *   <li>{@link io.agentscope.core.memory.StateBackedMemory} —— 状态背书的记忆，可持久化到 {@code AgentStateStore}</li>
 *   <li>{@code MemoryConfig} —— 记忆管理配置：flush、consolidation、session retention</li>
 * </ul>
 *
 * <p>本例通过 {@code HarnessAgent.Builder.memory(MemoryConfig)} 配置记忆管理策略，
 * 同一会话内消息会累积在记忆里，Agent 能"记住"上下文。
 * 对照组 {@link #chatNewSession} 用新 sessionId 演示记忆隔离。
 */
@Component
public class MemoryAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-memory";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public MemoryAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public String chat(String message) {
        return agent().call(new UserMessage(message), runtimeContext("memory-demo")).block().getTextContent();
    }

    /** 新会话——记忆隔离，不继承之前的上下文。 */
    public String chatNewSession(String message, String sessionId) {
        return agent().call(new UserMessage(message), runtimeContext(sessionId)).block().getTextContent();
    }

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    MemoryConfig memoryConfig = MemoryConfig.builder()
                            .model(model)
                            .sessionRetentionDays(7)
                            .build();
                    local = HarnessAgent.builder()
                            .name("memory-agent")
                            .sysPrompt("你是一个有记忆的助手，能记住同一会话内的上下文。")
                            .model(model)
                            .memory(memoryConfig)
                            .workspace(Paths.get(WORKSPACE_DIR))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext(String sessionId) {
        return RuntimeContext.builder()
                .sessionId(sessionId).userId("alice").build();
    }
}