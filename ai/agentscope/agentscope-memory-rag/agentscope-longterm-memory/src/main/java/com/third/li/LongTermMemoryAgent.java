package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.memory.LongTermMemory;
import io.agentscope.core.memory.Memory;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.memory.StaticLongTermMemoryHook;
import io.agentscope.core.hook.Hook;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 长期记忆 Agent：用 {@link LongTermMemory} + {@link StaticLongTermMemoryHook}
 * 跨会话记住用户信息。
 *
 * <p>AgentScope 的长期记忆是接口（{@link LongTermMemory}），需要提供具体实现——
 * 生产环境通常接 Mem0 / ReMe / 向量库。本例用内存实现演示 API：
 * <ul>
 *   <li>{@code record(msgs)} —— 对话结束后记录到长期记忆</li>
 *   <li>{@code retrieve(msg)} —— 下次对话前检索相关记忆，注入上下文</li>
 * </ul>
 *
 * <p>{@link StaticLongTermMemoryHook} 把记录/检索接入 ReAct 循环的
 * {@code PreCall} / {@code PostCall} 钩子，实现自动记忆。
 */
@Component
public class LongTermMemoryAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-longterm-memory";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public LongTermMemoryAgent(
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

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    LongTermMemory ltm = new InMemoryLongTermMemory();
                    Memory memory = new InMemoryMemory();
                    Hook longTermHook = new StaticLongTermMemoryHook(ltm, memory);

                    local = HarnessAgent.builder()
                            .name("longterm-memory")
                            .sysPrompt("你是一个有长期记忆的助手，能跨会话记住用户的偏好与信息。")
                            .model(model)
                            .hook(longTermHook)
                            .workspace(Paths.get(WORKSPACE_DIR))
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
     * 内存版 {@link LongTermMemory}：简单累加对话消息，检索时按关键词匹配。
     * 生产环境应替换为 Mem0 / ReMe / 向量库实现。
     */
    static class InMemoryLongTermMemory implements LongTermMemory {
        private final List<Msg> store = new ArrayList<>();

        @Override
        public reactor.core.publisher.Mono<Void> record(List<Msg> msgs) {
            return reactor.core.publisher.Mono.fromRunnable(() -> store.addAll(msgs));
        }

        @Override
        public reactor.core.publisher.Mono<String> retrieve(Msg query) {
            io.agentscope.core.message.TextBlock tb = query.getFirstContentBlock(io.agentscope.core.message.TextBlock.class);
            String text = tb != null ? tb.getText() : "";
            String memories = store.stream()
                    .map(m -> {
                        io.agentscope.core.message.TextBlock b = m.getFirstContentBlock(io.agentscope.core.message.TextBlock.class);
                        return b != null ? b.getText() : "";
                    })
                    .filter(t -> !t.isEmpty() && (text.isEmpty() || t.contains(text) || text.contains(t)))
                    .collect(Collectors.joining("\n"));
            return reactor.core.publisher.Mono.just(memories.isEmpty() ? "相关长期记忆" : memories);
        }
    }
}