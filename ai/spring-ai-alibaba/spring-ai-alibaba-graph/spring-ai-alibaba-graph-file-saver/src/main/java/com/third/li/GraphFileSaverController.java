package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * checkpoint 落盘接口。
 *
 * GET /graph/file-saver/chat?threadId=…&amp;message=… — 同一 thread 多轮对话（状态落盘）。
 * GET /graph/file-saver/history?threadId=… — 从磁盘恢复状态历史。
 */
@RestController
public class GraphFileSaverController {

    private final GraphFileSaverService service;

    public GraphFileSaverController(GraphFileSaverService service) {
        this.service = service;
    }

    @GetMapping("/graph/file-saver/chat")
    public Map<String, Object> chat(
            @RequestParam(value = "threadId", required = false) String threadId,
            @RequestParam(value = "message", defaultValue = "用一句话介绍 FileSystemSaver") String message)
            throws Exception {
        return service.chat(threadId, message);
    }

    @GetMapping("/graph/file-saver/history")
    public String history(@RequestParam("threadId") String threadId) throws Exception {
        return service.history(threadId);
    }
}
