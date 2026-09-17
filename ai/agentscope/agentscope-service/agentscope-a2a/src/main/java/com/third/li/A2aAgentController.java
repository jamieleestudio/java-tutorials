package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class A2aAgentController {

    private final A2aAgent agent;

    public A2aAgentController(A2aAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/a2a/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "A2A：Agent 间通信协议") String message) {
        return agent.chat(message);
    }
}