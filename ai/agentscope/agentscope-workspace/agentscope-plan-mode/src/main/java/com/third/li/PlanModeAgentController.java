package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class PlanModeAgentController {

    private final PlanModeAgent agent;

    public PlanModeAgentController(PlanModeAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/workspace/plan")
    public String ask(
            @RequestParam(value = "message", defaultValue = "计划模式：先规划再执行（类似 Claude Code 的 plan mode）") String message) {
        return agent.chat(message);
    }
}