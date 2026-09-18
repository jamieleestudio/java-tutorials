package com.third.li;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 流式输出（ChatModel.stream + Flux + SSE）。
 *
 * <p>Spring AI 2.0 用 Reactor {@link Flux} 做流式：
 * <ul>
 *   <li>{@code chatModel.stream(message)} — 每个元素是一段文本</li>
 *   <li>{@code chatModel.stream(prompt)} — 每个元素是完整 {@code ChatResponse}</li>
 * </ul>
 *
 * <p>Controller 直接返回 {@code Flux<String>}，Spring Boot 自动按 SSE 流式下发。
 */
@RestController
public class StreamingController {

    private final ChatModel chatModel;

    public StreamingController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /** 流式文本：Flux<String>，每个元素是一段生成文本。 */
    @GetMapping("/ai/streaming")
    public Flux<String> stream(
            @RequestParam(value = "message", defaultValue = "写一首关于春天的五言绝句") String message) {
        return chatModel.stream(message);
    }

    /** 流式 ChatResponse：每个元素包含完整响应元数据。 */
    @GetMapping("/ai/streaming/response")
    public Flux<org.springframework.ai.chat.model.ChatResponse> streamResponse(
            @RequestParam(value = "message", defaultValue = "数一下从 1 到 10") String message) {
        var prompt = new org.springframework.ai.chat.prompt.Prompt(
                new org.springframework.ai.chat.messages.UserMessage(message));
        return chatModel.stream(prompt);
    }
}
