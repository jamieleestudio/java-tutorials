package com.third.li;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 流式输出（ChatModel.stream + Flux + SSE）。
 *
 * <p>与 Spring AI 完全一致：Controller 返回 {@code Flux<String>}，
 * Spring Boot 自动按 SSE 流式下发。Graph / Agent Framework 的流式编排
 * 见 {@code spring-ai-alibaba-graph-streaming}。
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
    public Flux<ChatResponse> streamResponse(
            @RequestParam(value = "message", defaultValue = "数一下从 1 到 10") String message) {
        var prompt = new Prompt(new UserMessage(message));
        return chatModel.stream(prompt);
    }
}
