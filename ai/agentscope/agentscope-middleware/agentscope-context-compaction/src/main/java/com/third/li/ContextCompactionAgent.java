package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 上下文压缩 Agent：通过 {@link HarnessAgent.Builder#compaction(CompactionConfig)}
 * 配置 {@link CompactionConfig}，当对话 token 超过阈值时自动摘要压缩历史。
 *
 * <p>{@link CompactionConfig} 的关键参数：
 * <ul>
 *   <li>{@code triggerTokens} —— 触发压缩的 token 阈值（估算）</li>
 *   <li>{@code keepMessages} / {@code keepTokens} —— 压缩后保留的最近消息数/token 数</li>
 *   <li>{@code summaryPrompt} —— 摘要时用的提示词</li>
 * </ul>
 *
 * <p>对照组 {@link #chatWithoutCompaction} 用默认配置（压缩关闭）的 Agent，
 * 便于观察长对话时两者的行为差异。
 */
@Component
public class ContextCompactionAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-context-compaction";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;
    private volatile HarnessAgent plainAgent;

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

    public String chatWithoutCompaction(String message) {
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
                    CompactionConfig compaction = CompactionConfig.builder()
                            .triggerTokens(2000)
                            .keepMessages(4)
                            .keepTokens(800)
                            .summaryPrompt("请用中文简洁总结以下对话的关键信息，保留用户意图和已确定结论。")
                            .flushBeforeCompact(true)
                            .build();
                    local = HarnessAgent.builder()
                            .name("context-compaction")
                            .sysPrompt("你是一个乐于助人的中文智能助手。")
                            .model(model)
                            .compaction(compaction)
                            .workspace(Paths.get(WORKSPACE_DIR))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private HarnessAgent plainAgent() {
        HarnessAgent local = plainAgent;
        if (local == null) {
            synchronized (this) {
                local = plainAgent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("context-compaction-plain")
                            .sysPrompt("你是一个乐于助人的中文智能助手。")
                            .model(model)
                            .disableCompaction()
                            .workspace(Paths.get(WORKSPACE_DIR))
                            .build();
                    plainAgent = local;
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