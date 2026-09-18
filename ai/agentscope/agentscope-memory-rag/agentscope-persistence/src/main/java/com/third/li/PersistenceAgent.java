package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.core.state.JsonFileAgentStateStore;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 持久化 Agent：用 {@link JsonFileAgentStateStore} 把 Agent 状态持久化到磁盘，
 * 支持跨重启恢复会话。
 *
 * <p>AgentScope 的持久化两层：
 * <ul>
 *   <li>{@link AgentStateStore} —— 存储 {@code AgentState}（消息、工具上下文、任务、权限等）</li>
 *   <li>会话持久化 —— 通过 {@code HarnessAgent.Builder.stateStore(store)} 注入；
 *       默认开启，可用 {@code disableSessionPersistence()} 关闭</li>
 * </ul>
 *
 * <p>{@link JsonFileAgentStateStore} 把状态以 JSON 文件存到指定目录。
 * 重启后同一 sessionId/userId 的 Agent 能恢复上下文。
 *
 * <p>对照组 {@link #chatWithoutPersistence} 用内存 store，重启即丢。
 */
@Component
public class PersistenceAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-persistence";
    private static final String STATE_DIR = ".agentscope/state-persistence";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;
    private volatile HarnessAgent ephemeralAgent;

    public PersistenceAgent(
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

    public String chatWithoutPersistence(String message) {
        return ephemeralAgent().call(new UserMessage(message), runtimeContext()).block().getTextContent();
    }

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    AgentStateStore store = new JsonFileAgentStateStore(
                            Paths.get(STATE_DIR).toAbsolutePath());
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("persistence")
                            .sysPrompt("你是一个有持久化记忆的助手，重启后能恢复会话上下文。")
                            .model(model)
                            .stateStore(store)
                            .workspace(Paths.get(WORKSPACE_DIR))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private HarnessAgent ephemeralAgent() {
        HarnessAgent local = ephemeralAgent;
        if (local == null) {
            synchronized (this) {
                local = ephemeralAgent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("persistence-ephemeral")
                            .sysPrompt("你是一个助手，会话不持久化。")
                            .model(model)
                            .disableSessionPersistence()
                            .workspace(Paths.get(WORKSPACE_DIR))
                            .build();
                    ephemeralAgent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("persistence-demo").userId("alice").build();
    }
}