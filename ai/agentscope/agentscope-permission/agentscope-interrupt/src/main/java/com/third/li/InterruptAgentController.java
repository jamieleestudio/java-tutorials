package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class InterruptAgentController {

    private final InterruptAgent agent;

    public InterruptAgentController(InterruptAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/interrupt/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "实时打断：Agent 运行中可以 interrupt() 中止") String message) {
        return agent.chat(message);
    }
}