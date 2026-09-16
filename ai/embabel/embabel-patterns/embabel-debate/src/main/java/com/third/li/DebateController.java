package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 辩论接口。
 */
@RestController
public class DebateController {

    private final AgentPlatform agentPlatform;

    public DebateController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/debate/ask")
    public DebateResult ask(
            @RequestParam(value = "topic", defaultValue = "初创团队是否应该直接上微服务架构？") String topic) {
        return AgentInvocation.create(agentPlatform, DebateResult.class).invoke(new UserInput(topic));
    }
}
