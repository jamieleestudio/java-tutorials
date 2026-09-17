package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class ContextCompactionAgentController {

    private final ContextCompactionAgent agent;

    public ContextCompactionAgentController(ContextCompactionAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/compaction/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "上下文压缩中间件：当对话历史超过阈值时自动压缩，避免 token 爆炸") String message) {
        return agent.chat(message);
    }
}