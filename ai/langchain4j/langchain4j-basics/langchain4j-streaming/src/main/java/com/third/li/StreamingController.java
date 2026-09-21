package com.third.li;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import dev.langchain4j.service.TokenStream;

/**
 * 流式输出接口：TokenStream → SSE。
 */
@RestController
public class StreamingController {

    private final StreamingAiConfig.StreamingWriter writer;

    public StreamingController(StreamingAiConfig.StreamingWriter writer) {
        this.writer = writer;
    }

    @GetMapping(value = "/ai/streaming", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam(defaultValue = "春天的公园") String topic) {
        SseEmitter emitter = new SseEmitter(120_000L);
        TokenStream tokenStream = writer.write(topic);
        tokenStream.onPartialResponse(token -> {
                    try {
                        emitter.send(token);
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                })
                .onCompleteResponse(response -> emitter.complete())
                .onError(error -> emitter.completeWithError(error))
                .start();
        return emitter;
    }
}
