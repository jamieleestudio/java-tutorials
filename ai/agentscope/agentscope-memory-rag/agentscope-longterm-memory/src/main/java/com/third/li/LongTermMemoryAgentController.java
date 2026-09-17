package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class LongTermMemoryAgentController {

    private final LongTermMemoryAgent agent;

    public LongTermMemoryAgentController(LongTermMemoryAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/longterm/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "长期记忆：跨会话持久化记忆（需要外部后端）") String message) {
        return agent.chat(message);
    }
}