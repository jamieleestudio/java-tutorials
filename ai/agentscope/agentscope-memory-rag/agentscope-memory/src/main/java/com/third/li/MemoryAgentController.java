package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class MemoryAgentController {

    private final MemoryAgent agent;

    public MemoryAgentController(MemoryAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/memory/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "内存记忆：会话内的消息历史管理") String message) {
        return agent.chat(message);
    }
}