package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class CapstoneE2eAgentController {

    private final CapstoneE2eAgent agent;

    public CapstoneE2eAgentController(CapstoneE2eAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/capstone/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "端到端：permission+workspace+skill+subagent 串起来") String message) {
        return agent.chat(message);
    }
}