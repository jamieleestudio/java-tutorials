package com.third.li;

import io.agentscope.core.agent.Event;
import io.agentscope.core.agent.EventType;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.TextBlock;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 流式输出接口（SSE）。
 *
 * <p>两个端点：
 * <ul>
 *   <li>{@code /stream/text} — 只返回文本增量（纯 String SSE）</li>
 *   <li>{@code /stream/events} — 返回完整事件流（含类型标注，能看到 thinking/tool/summary 的区别）</li>
 * </ul>
 */
@RestController
public class StreamingController {

    private final StreamingAgent streamingAgent;

    public StreamingController(StreamingAgent streamingAgent) {
        this.streamingAgent = streamingAgent;
    }

    /** 纯文本流式（像 Embabel 的 conversation 流式端点）。 */
    @GetMapping(value = "/stream/text", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamText(
            @RequestParam(value = "message", defaultValue = "用三句话介绍 AgentScope") String message) {
        return streamingAgent.streamText(message);
    }

    /**
     * 完整事件流式（AgentScope 独有）。
     *
     * <p>每条 SSE 数据格式：{@code [事件类型] 文本增量}
     * 可以看到 REASONING（思考）→ SUMMARY（最终回复）的分阶段输出。
     */
    @GetMapping(value = "/stream/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamEvents(
            @RequestParam(value = "message", defaultValue = "用三句话介绍 AgentScope") String message) {
        return streamingAgent.stream(message)
                .map(event -> {
                    EventType type = event.getType();
                    Msg msg = event.getMessage();
                    if (msg == null) {
                        return "[%s]".formatted(type);
                    }
                    TextBlock textBlock = msg.getFirstContentBlock(TextBlock.class);
                    String text = textBlock != null ? textBlock.getText() : "";
                    return "[%s] %s".formatted(type, text);
                });
    }
}