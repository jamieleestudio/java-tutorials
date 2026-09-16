package com.third.li;

import com.embabel.agent.api.common.AiBuilder;
import com.embabel.agent.api.common.PromptRunner;
import com.embabel.agent.api.common.streaming.StreamingPromptRunner;
import com.embabel.chat.AssistantMessage;
import com.embabel.chat.UserMessage;
import com.embabel.chat.support.InMemoryConversation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 流式对话接口（SSE）——**带记忆的流式输出**。
 *
 * <p>两个关键点：
 * <ol>
 *   <li><b>先探测再流式</b>：{@code supportsStreaming()} 为 false 时降级为一次性返回，
 *       避免在模型/Provider 不支持时抛异常。</li>
 *   <li><b>流式也要记住上下文</b>：把会话历史通过 {@code withMessages(history)} 传入，
 *       并在流结束时把**拼装好的完整回复**写回历史，
 *       这样流式接口与阻塞式接口可以混用（共用 {@link ConversationStore}）。</li>
 * </ol>
 *
 * <p>因为 {@code Flux} 是冷流（订阅时才执行），这里用 {@code Flux.defer(...)}
 * 把"追加用户消息 + 读取历史"推迟到订阅时刻，避免重复订阅时历史错乱。
 */
@RestController
@RequestMapping("/chat")
public class StreamingController {

    private final AiBuilder aiBuilder;
    private final ConversationStore store;

    public StreamingController(AiBuilder aiBuilder, ConversationStore store) {
        this.aiBuilder = aiBuilder;
        this.store = store;
    }

    @GetMapping(value = "/{sessionId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(
            @PathVariable String sessionId,
            @RequestParam(value = "message") String message) {
        return Flux.defer(() -> {
            InMemoryConversation conversation = store.get(sessionId);
            conversation.addMessage(new UserMessage(message));

            PromptRunner runner = aiBuilder.ai()
                    .withDefaultLlm()
                    .withSystemPrompt(ChatController.SYSTEM_PROMPT);

            // 不支持流式：降级为一次性返回，但仍然记住上下文
            if (!runner.supportsStreaming()) {
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
}
