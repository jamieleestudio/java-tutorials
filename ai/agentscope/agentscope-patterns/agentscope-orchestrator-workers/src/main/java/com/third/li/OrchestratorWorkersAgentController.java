package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 编排者-工人接口。 */
@RestController
public class OrchestratorWorkersAgentController {

    private final OrchestratorWorkersAgent agent;

    public OrchestratorWorkersAgentController(OrchestratorWorkersAgent agent) {
        this.agent = agent;
    }

    /** 主 Agent 编排：拆解任务并委派给工人子 Agent。 */
    @GetMapping("/patterns/orchestrator/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "帮我写一个简单的计算器程序并审查代码质量") String message) {
        return agent.chat(message);
    }
}