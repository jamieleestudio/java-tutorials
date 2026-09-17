package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 端到端多 Agent 系统接口。 */
@RestController
public class E2ECapstoneAgentController {

    private final E2ECapstoneAgent agent;

    public E2ECapstoneAgentController(E2ECapstoneAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/capstone/e2e")
    public String ask(
            @RequestParam(value = "message", defaultValue = "帮我研究 AgentScope 的中间件设计并生成一份技术报告") String message) {
        return agent.chat(message);
    }
}