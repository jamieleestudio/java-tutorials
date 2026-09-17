package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class PersistenceAgentController {

    private final PersistenceAgent agent;

    public PersistenceAgentController(PersistenceAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/persistence/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "持久化：Agent 状态/会话落库") String message) {
        return agent.chat(message);
    }
}