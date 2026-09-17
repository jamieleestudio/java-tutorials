package com.third.li;

import io.agentscope.core.agent.Event;
import io.agentscope.core.agent.EventType;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.agent.StreamOptions;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 流式 Agent：用 {@code agent.stream(msg, options, ctx)} 返回 {@code Flux<Event>}，
 * 每个事件携带一小段增量内容（TextBlock / ThinkingBlock / ToolResultBlock 等）。
 *
 * <p>与 Embabel 的对照：
 * <ul>
 *   <li>Embabel: {@code runner.streaming().withPrompt(msg).generateStream()} → {@code Flux<String>}</li>
 *   <li>AgentScope: {@code agent.stream(msg, options, ctx)} → {@code Flux<Event>}，
 *       Event 比 String 更细粒度——区分了 REASONING / SUMMARY / TOOL_RESULT / AGENT_RESULT</li>
 * </ul>
 */
@Component
public class StreamingAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public StreamingAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    /**
     * 流式调用，返回事件流。
     *
     * <p>每个 {@link Event} 的 {@code getType()} 是 {@link EventType} 之一：
     * <ul>
     *   <li>{@code REASONING} — 模型的思考过程（thinking）</li>
     *   <li>{@code SUMMARY} — 最终回复的文本增量</li>
     *   <li>{@code TOOL_RESULT} — 工具调用结果</li>
     *   <li>{@code AGENT_RESULT} — 整轮结束</li>
     * </ul>
     */
    public Flux<Event> stream(String message) {
        return agent().stream(
                        java.util.List.of(new UserMessage(message)),
                        StreamOptions.defaults(),
                        runtimeContext());
    }

    /** 把 Event 流转成纯文本增量流（只取 SUMMARY 的 TextBlock）。 */
    public Flux<String> streamText(String message) {
        return stream(message)
                .filter(event -> event.getType() == EventType.SUMMARY)
                .map(Event::getMessage)
                .filter(msg -> msg != null)
                .map(msg -> msg.getFirstContentBlock(TextBlock.class))
                .filter(block -> block != null)
                .map(TextBlock::getText);
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
                            .name("streaming-assistant")
                            .sysPrompt("你是一个乐于助人的中文智能助手，请简洁、准确地回答用户的问题。")
                            .model(model)
                            .workspace(java.nio.file.Paths.get(".agentscope/workspace-streaming"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("streaming-demo").userId("alice").build();
    }
}