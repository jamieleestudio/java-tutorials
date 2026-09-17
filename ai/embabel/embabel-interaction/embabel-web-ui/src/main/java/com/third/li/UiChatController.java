package com.third.li;

import com.embabel.agent.api.common.AiBuilder;
import com.embabel.agent.api.common.PromptRunner;
import com.embabel.agent.api.common.streaming.StreamingPromptRunner;
import com.embabel.chat.AssistantMessage;
import com.embabel.chat.UserMessage;
import com.embabel.chat.support.InMemoryConversation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 最小 Web UI 的后端：一个会话 + 一个 SSE 流式端点。
 *
 * <p>刻意保持极简——它的价值在于**让 48 个模块的 curl 变成可点的页面**，
 * 而不是再教一遍对话记忆（那部分见 {@code embabel-conversation}）。
 */
@RestController
@RequestMapping("/ui")
public class UiChatController {

    private static final Logger log = LoggerFactory.getLogger(UiChatController.class);

    private static final String DEFAULT_PERSONA =
            "你是 Embabel 教程里的助手。回答简洁、准确，用中文，必要时给出可运行的命令或代码。";

    private final AiBuilder aiBuilder;
    private final Map<String, InMemoryConversation> sessions = new ConcurrentHashMap<>();

    public UiChatController(AiBuilder aiBuilder) {
        this.aiBuilder = aiBuilder;
    }

    /** 阻塞式一轮对话（用于不支持流式的兜底，也方便脚本调用）。 */
    @PostMapping("/chat")
    public Map<String, Object> chat(
            @RequestParam(value = "sessionId", defaultValue = "default") String sessionId,
            @RequestParam(value = "message") String message,
            @RequestParam(value = "persona", required = false) String persona) {
        InMemoryConversation conversation = conversation(sessionId);
        conversation.addMessage(new UserMessage(message));

        PromptRunner runner = aiBuilder.ai().withDefaultLlm()
                .withSystemPrompt(persona == null || persona.isBlank() ? DEFAULT_PERSONA : persona);
        AssistantMessage reply = runner.respond(conversation.getMessages());
        conversation.addMessage(reply);

        return Map.of("sessionId", sessionId, "reply", reply.getContent(),
                "messageCount", conversation.getMessages().size());
    }

    /**
     * SSE 流式对话（页面用 {@code EventSource} 调它）。
     *
     * <p>与 {@code embabel-conversation} 同样的两个要点：
     * <b>先探测</b> {@code supportsStreaming()} 再决定是否流式；
     * 流结束时把**拼装好的完整回复**写回历史。
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(
            @RequestParam(value = "sessionId", defaultValue = "default") String sessionId,
            @RequestParam(value = "message") String message,
            @RequestParam(value = "persona", required = false) String persona) {
        return Flux.defer(() -> {
            InMemoryConversation conversation = conversation(sessionId);
            conversation.addMessage(new UserMessage(message));

            PromptRunner runner = aiBuilder.ai().withDefaultLlm()
                    .withSystemPrompt(persona == null || persona.isBlank() ? DEFAULT_PERSONA : persona);

            if (!runner.supportsStreaming()) {
                log.info("当前模型不支持流式，降级为一次性返回");
                AssistantMessage reply = runner.respond(conversation.getMessages());
                conversation.addMessage(reply);
                return Flux.just(reply.getContent());
            }

            StringBuilder assembled = new StringBuilder();
            return ((StreamingPromptRunner.Streaming) runner.streaming())
                    .withMessages(conversation.getMessages())
                    .generateStream()
                    .doOnNext(assembled::append)
                    .doOnComplete(() ->
                            conversation.addMessage(new AssistantMessage(assembled.toString())));
        });
    }

    /** 清空某个会话。 */
    @PostMapping("/reset")
    public Map<String, Object> reset(@RequestParam(value = "sessionId", defaultValue = "default") String sessionId) {
        sessions.remove(sessionId);
        return Map.of("sessionId", sessionId, "reset", true);
    }

    private InMemoryConversation conversation(String sessionId) {
        return sessions.computeIfAbsent(sessionId, id -> new InMemoryConversation(List.of(), id));
    }
}
