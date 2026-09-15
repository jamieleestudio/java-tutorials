package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 规划器对比接口。
 */
@RestController
public class PlannerController {

    private final AgentPlatform agentPlatform;

    public PlannerController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/planner/goap")
    public GoapAnswer goap(
            @RequestParam(value = "message", defaultValue = "为什么要给 Agent 做类型化建模？") String message) {
        AgentProcess process = AgentInvocation.create(agentPlatform, GoapAnswer.class).run(new UserInput(message));
        return process.last(GoapAnswer.class);
    }

    @GetMapping("/planner/utility")
    public UtilityAnswer utility(
            @RequestParam(value = "message", defaultValue = "我该怎么规划一个 Agent 项目？") String message) {
        AgentProcess process = AgentInvocation.create(agentPlatform, UtilityAnswer.class).run(new UserInput(message));
        return process.last(UtilityAnswer.class);
    }
}
