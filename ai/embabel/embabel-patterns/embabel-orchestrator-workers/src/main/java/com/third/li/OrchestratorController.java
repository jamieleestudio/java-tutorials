package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 编排者-工人接口。
 */
@RestController
public class OrchestratorController {

    private final AgentPlatform agentPlatform;

    public OrchestratorController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/orchestrator/ask")
    public FinalReport ask(
            @RequestParam(value = "message", defaultValue = "我们该不该把单体拆成微服务？") String message) {
        return AgentInvocation.create(agentPlatform, FinalReport.class).invoke(new UserInput(message));
    }
}
