package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 上下文压缩（CompactionMiddleware）。
 *
 * <p>AgentScope 内置 {@link io.agentscope.harness.agent.middleware.CompactionMiddleware}，
 * 当对话历史超过阈值（消息数 / token 数）时，自动用模型生成摘要，
 * 替换旧消息，避免上下文窗口爆炸。
 *
 * <p>通过 {@link HarnessAgent.Builder#compaction(CompactionConfig)} 配置：
 * <ul>
 *   <li>{@code triggerMessages} — 消息数阈值（超过则压缩）</li>
 *   <li>{@code triggerTokens} — token 数阈值</li>
 *   <li>{@code keepMessages} — 保留最近几条消息不压缩</li>
 *   <li>{@code keepTokensRatio} — 保留比例</li>
 * </ul>
 *
 * <p>与 Embabel 的差异：Embabel 没有内置上下文压缩——
 * 它依赖 process 的 goal 满足机制自然终止，不处理长对话。
 * AgentScope 的 CompactionMiddleware 是"对话内"压缩。
 */
@Component
public class ContextCompactionAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public ContextCompactionAgent(
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
                    CompactionConfig config = CompactionConfig.builder()
                            .triggerMessages(8)
                            .triggerTokens(2000)
                            .keepMessages(4)
                            .keepTokensRatio(0.3)
                            .build();
                    local = HarnessAgent.builder()
                            .name("context-compaction")
                            .sysPrompt("你是一个乐于助人的中文智能助手。")
                            .model(model)
                            .compaction(config)
                            .workspace(java.nio.file.Paths.get(".agentscope/workspace-context-compaction"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("compaction-demo").userId("alice").build();
    }
}