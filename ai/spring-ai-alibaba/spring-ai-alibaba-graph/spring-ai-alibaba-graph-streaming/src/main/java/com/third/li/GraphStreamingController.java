package com.third.li;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 图级流式接口：GET /graph/stream，SSE 下发 token 增量与节点事件。
 */
@RestController
public class GraphStreamingController {

    private final GraphStreamingService graphService;

    public GraphStreamingController(GraphStreamingService graphService) {
        this.graphService = graphService;
    }

    @GetMapping(value = "/graph/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(
            @RequestParam(value = "message", defaultValue = "春天的公园") String message) {
        return graphService.stream(message)
                .map(chunk -> ServerSentEvent.<String>builder().data(chunk).build());
    }
}
