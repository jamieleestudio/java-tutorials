package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 状态 checkpoint 接口。
 *
 * <p>GET /graph/checkpoint/chat?threadId=xxx&amp;message=… — 在同一 thread 上多轮执行。
 * GET /graph/checkpoint/history?threadId=xxx — 回看线程的每一步状态快照。
 */
@RestController
public class GraphCheckpointController {

    private final GraphCheckpointService graphService;

    public GraphCheckpointController(GraphCheckpointService graphService) {
        this.graphService = graphService;
    }

    @GetMapping("/graph/checkpoint/chat")
    public Map<String, Object> chat(
            @RequestParam(value = "threadId", required = false) String threadId,
            @RequestParam(value = "message", defaultValue = "你好，介绍一下你自己") String message) throws Exception {
        return graphService.chat(threadId, message);
    }

    @GetMapping("/graph/checkpoint/history")
    public List<String> history(@RequestParam("threadId") String threadId) {
        return graphService.history(threadId);
    }
}
