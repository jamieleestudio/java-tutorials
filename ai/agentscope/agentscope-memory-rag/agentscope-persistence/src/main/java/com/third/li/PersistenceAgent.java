package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.core.state.InMemoryAgentStateStore;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 会话持久化（AgentStateStore + sessionPersistence）。
 *
 * <p>AgentScope 的会话持久化通过 {@link AgentStateStore} 实现：
 * <ul>
 *   <li>{@link InMemoryAgentStateStore} — 内存存储（重启丢失）</li>
 *   <li>文件系统存储 — WorkspaceManager 管理（默认启用）</li>
 *   <li>分布式存储 — DistributedStore（Redis / 数据库）</li>
 * </ul>
 *
 * <p>通过 {@code .stateStore(store)} 注入自定义存储。
 * 默认情况下 AgentScope 使用工作区文件系统做持久化。
 *
 * <p>同一 userId + sessionId 的多次调用会恢复之前的对话上下文。
 * 调用 {@code agent.clearContext(ctx)} 清除会话。
 */
@Component
public class PersistenceAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;
    private final AgentStateStore stateStore = new InMemoryAgentStateStore();

    public PersistenceAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    /** 同一 session 的多次调用会保持上下文。 */
    public String chat(String message, String sessionId) {
        RuntimeContext ctx = RuntimeContext.builder()
                .sessionId(sessionId).userId("alice").build();
        return agent().call(new UserMessage(message), ctx).block().getTextContent();
    }

    /** 清除指定会话。 */
    public String clearSession(String sessionId) {
        agent().clearContext(sessionId, "alice");
        return "已清除会话：" + sessionId;
    }

    /** 列出所有会话。 */
    public String listSessions() {
        var ids = stateStore.listSessionIds("alice");
        return ids.isEmpty() ? "无会话" : "会话列表：" + String.join(", ", ids);
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
                            .name("persistence")
                            .sysPrompt("你是一个有持久化记忆的助手。同一会话的多次对话你会记住上下文。")
                            .model(model)
                            .stateStore(stateStore)
                            .workspace(Paths.get(".agentscope/workspace-persistence"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }
}