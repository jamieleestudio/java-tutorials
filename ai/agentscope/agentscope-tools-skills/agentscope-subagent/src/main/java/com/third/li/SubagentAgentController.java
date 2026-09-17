package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class SubagentAgentController {

    private final SubagentAgent agent;

    public SubagentAgentController(SubagentAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/subagent/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "子代理：把另一个 Agent 包装成工具，模型可以委派任务") String message) {
        return agent.chat(message);
    }
}