package com.third.li;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.pii.PIIDetectionHook;
import com.alibaba.cloud.ai.graph.agent.hook.pii.PIIDetectors;
import com.alibaba.cloud.ai.graph.agent.hook.pii.PIIType;
import com.alibaba.cloud.ai.graph.agent.hook.pii.RedactionStrategy;
import com.alibaba.cloud.ai.graph.agent.hook.summarization.SummarizationHook;
import com.alibaba.cloud.ai.graph.agent.hook.toolcalllimit.ToolCallLimitHook;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

/**
 * Hook 体系：模型调用前后的横切逻辑，可自由组合。
 *
 * <p>本模块组合三个内置 Hook：
 * <ul>
 *   <li>{@link SummarizationHook} — 上下文自动摘要：消息 token 超过阈值时，
 *       用模型把旧消息压缩成摘要（保留最近 N 条），防止长会话上下文爆炸</li>
 *   <li>{@link PIIDetectionHook} — 敏感信息检测：用 {@link PIIDetectors} 正则检测
 *       邮箱等 PII，按 {@link RedactionStrategy#MASK} 打码后再进入模型</li>
 *   <li>{@link ToolCallLimitHook} — 工具调用限流：单个工具在会话内最多调用 N 次</li>
 * </ul>
 *
 * <p>对照 {@code agentscope-middleware}：SAA 的 Hook ≈ AgentScope 的 Middleware，
 * 但挂载点是"模型调用前后"而非完整请求链。
 */
@Service
public class AgentHooksService {

    private final ReactAgent summarizationAgent;
    private final ReactAgent piiAgent;

    public AgentHooksService(ChatModel chatModel) throws GraphStateException {
        // Hook 1：上下文自动摘要（阈值用字符数近似 token，演示用）
        this.summarizationAgent = ReactAgent.builder()
                .name("summarization-agent")
                .description("长对话助手：自动摘要旧上下文")
                .systemPrompt("你是长对话助手，回答简短。")
                .model(chatModel)
                .hooks(SummarizationHook.builder()
                        .model(chatModel)
                        .maxTokensBeforeSummary(500)
                        .messagesToKeep(2)
                        .summaryPrefix("【历史摘要】")
                        .build())
                .build();

        // Hook 2 + 3：PII 打码 + 工具限流
        this.piiAgent = ReactAgent.builder()
                .name("pii-agent")
                .description("敏感信息防护助手")
                .systemPrompt("你是客服助手，回答简短。用户消息里的敏感信息会被自动打码。")
                .model(chatModel)
                .hooks(
                        PIIDetectionHook.builder()
                                .piiType(PIIType.EMAIL)
                                .detector(PIIDetectors.emailDetector())
                                .strategy(RedactionStrategy.MASK)
                                .applyToInput(true)
                                .applyToOutput(true)
                                .build(),
                        ToolCallLimitHook.builder()
                                .threadLimit(10)
                                .exitBehavior(ToolCallLimitHook.ExitBehavior.END)
                                .build())
                .build();
    }

    /** 长对话演示：同一会话多轮输入，观察旧消息被摘要替换。 */
    public String chat(Long round, String message) throws Exception {
        var config = com.alibaba.cloud.ai.graph.RunnableConfig.builder()
                .threadId("hooks-demo").build();
        return summarizationAgent.call("(第" + round + "轮) " + message, config).getText();
    }

    /** PII 演示：输入包含邮箱，观察输出中的打码效果。 */
    public String pii(String message) throws Exception {
        return piiAgent.call(message).getText();
    }
}
