package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 计划模式接口。 */
@RestController
public class PlanModeAgentController {

    private final PlanModeAgent agent;

    public PlanModeAgentController(PlanModeAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/plan/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "分析这个项目的架构并制定重构计划") String message) {
        return agent.chat(message);
    }

    @GetMapping("/plan/enter")
    public String enter() { return agent.enterPlanMode(); }

    @GetMapping("/plan/exit")
    public String exit() { return agent.exitPlanMode(); }

    @GetMapping("/plan/status")
    public String status() { return agent.checkPlanMode(); }
}