package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 引用加载接口。
 */
@RestController
public class ReferencesController {

    private final AgentPlatform agentPlatform;

    public ReferencesController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/references/ask")
    public Answer ask(
            @RequestParam(value = "message", defaultValue = "动作（Action）和黑板（Blackboard）分别是什么？") String message) {
        return AgentInvocation.create(agentPlatform, Answer.class).invoke(new UserInput(message));
    }
}
