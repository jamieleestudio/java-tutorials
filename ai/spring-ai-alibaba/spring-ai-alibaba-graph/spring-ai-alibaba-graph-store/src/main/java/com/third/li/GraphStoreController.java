package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 长期记忆 Store 接口。
 *
 * GET /graph/store/chat?threadId=…&amp;message=记住:favorite=Graph
 * GET /graph/store/dump — 查看 Store 全量。
 */
@RestController
public class GraphStoreController {

    private final GraphStoreService service;

    public GraphStoreController(GraphStoreService service) {
        this.service = service;
    }

    @GetMapping("/graph/store/chat")
    public Map<String, Object> chat(
            @RequestParam(value = "threadId", required = false) String threadId,
            @RequestParam(value = "message", defaultValue = "记住:favorite=Graph 编排") String message)
            throws Exception {
        return service.chat(threadId, message);
    }

    @GetMapping("/graph/store/dump")
    public String dump() {
        return service.dump();
    }
}
