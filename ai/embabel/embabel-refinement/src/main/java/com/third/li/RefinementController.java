package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 自评迭代接口。
 */
@RestController
public class RefinementController {

    private final AgentPlatform agentPlatform;

    public RefinementController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/refine")
    public RefinedText refine(
            @RequestParam(value = "message",
                    defaultValue = "写一段面向新用户的 Embabel 简介，要求点明它和普通工作流框架的区别。") String message) {
        return AgentInvocation.create(agentPlatform, RefinedText.class).invoke(new UserInput(message));
    }
}
