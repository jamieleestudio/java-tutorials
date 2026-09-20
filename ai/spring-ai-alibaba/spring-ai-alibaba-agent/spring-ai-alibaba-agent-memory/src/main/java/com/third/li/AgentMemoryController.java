package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agent 记忆接口：GET /agent/memory/chat?threadId=…&amp;message=…
 *
 * 第一轮不传 threadId（返回新会话 id），后续轮次带上同一 threadId 即可续聊。
 */
@RestController
public class AgentMemoryController {

    private final AgentMemoryService agentService;

    public AgentMemoryController(AgentMemoryService agentService) {
        this.agentService = agentService;
    }

    @GetMapping("/agent/memory/chat")
    public String chat(
            @RequestParam(value = "threadId", required = false) String threadId,
            @RequestParam(value = "message", defaultValue = "你好！我叫小李，最喜欢 Java。") String message)
            throws Exception {
        return agentService.chat(threadId, message);
    }
}
