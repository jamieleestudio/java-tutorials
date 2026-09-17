package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.memory.LongTermMemory;
import io.agentscope.core.memory.LongTermMemoryTools;
import io.agentscope.core.memory.Memory;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.memory.StaticLongTermMemoryHook;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.List;

/**
 * 长期记忆（LongTermMemory + StaticLongTermMemoryHook + LongTermMemoryTools）。
 *
 * <p>AgentScope 的长期记忆是<b>跨会话</b>的知识存储：
 * <ul>
 *   <li>{@link LongTermMemory} — 接口：record(消息列表) / retrieve(查询)</li>
 *   <li>{@link StaticLongTermMemoryHook} — Hook：在 Agent 事件时自动检索/记录</li>
 *   <li>{@link LongTermMemoryTools} — 工具：让 Agent 主动调用 record/retrieve</li>
 * </ul>
 *
 * <p>两种模式（{@code LongTermMemoryMode}）：
 * <ul>
 *   <li>{@code STATIC_CONTROL} — Hook 自动管理（无需 Agent 参与）</li>
 *   <li>{@code AGENT_CONTROL} — Agent 通过工具主动管理</li>
 *   <li>{@code BOTH} — 两者都启用</li>
 * </ul>
 *
 * <p>本模块演示 STATIC_CONTROL：用简单 InMemoryMemory 实现一个 LongTermMemory。
 */
@Component
public class LongTermMemoryAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;
    private final Memory longTermStore = new InMemoryMemory();

    public LongTermMemoryAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

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
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();

                    LongTermMemory ltm = new SimpleLongTermMemory(longTermStore);
                    StaticLongTermMemoryHook hook = new StaticLongTermMemoryHook(ltm, longTermStore, true);

                    local = HarnessAgent.builder()
                            .name("longterm-memory")
                            .sysPrompt("你是一个有长期记忆的助手。你会记住跨会话的重要信息。")
                            .model(model)
                            .hook(hook)
                            .workspace(Paths.get(".agentscope/workspace-longterm"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("ltm-demo").userId("alice").build();
    }

    /**
     * 简单的 LongTermMemory 实现——用 InMemoryMemory 存储。
     * 生产环境应替换为向量数据库实现。
     */
    static class SimpleLongTermMemory implements LongTermMemory {
        private final Memory store;

        SimpleLongTermMemory(Memory store) { this.store = store; }

        @Override
        public reactor.core.publisher.Mono<Void> record(List<io.agentscope.core.message.Msg> msgs) {
            msgs.forEach(store::addMessage);
            return reactor.core.publisher.Mono.empty();
        }

        @Override
        public reactor.core.publisher.Mono<String> retrieve(io.agentscope.core.message.Msg query) {
            StringBuilder sb = new StringBuilder();
            for (io.agentscope.core.message.Msg msg : store.getMessages()) {
                sb.append(msg.getTextContent()).append("\n");
            }
            return reactor.core.publisher.Mono.just(sb.length() > 0 ? sb.toString() : "(无长期记忆)");
        }
    }
}