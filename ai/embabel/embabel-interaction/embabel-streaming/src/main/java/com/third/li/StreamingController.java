package com.third.li;

import com.embabel.agent.api.common.AiBuilder;
import com.embabel.agent.api.common.PromptRunner;
import com.embabel.agent.api.common.streaming.StreamingPromptRunner;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 流式输出接口（SSE）。
 *
 * <p>先检查 {@code supportsStreaming()}，支持则把模型输出作为 {@code Flux<String>}
 * 逐块推送；否则降级为一次性返回，避免在模型/Provider 不支持时抛异常。
 */
@RestController
public class StreamingController {

    private final AiBuilder aiBuilder;

    public StreamingController(AiBuilder aiBuilder) {
        this.aiBuilder = aiBuilder;
    }

    @GetMapping(value = "/stream/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generate(
            @RequestParam(value = "message", defaultValue = "用三句话介绍 Embabel Agent Framework") String message) {
        PromptRunner runner = aiBuilder.ai().withDefaultLlm();
        if (!runner.supportsStreaming()) {
            return Flux.just(runner.generateText(message));
        }
        StreamingPromptRunner.Streaming streaming = (StreamingPromptRunner.Streaming) runner.streaming();
        return streaming.withPrompt(message).generateStream();
    }
}
