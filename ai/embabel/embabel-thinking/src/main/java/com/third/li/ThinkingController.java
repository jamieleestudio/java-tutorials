package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * thinking 提取接口。
 */
@RestController
public class ThinkingController {

    private final AgentPlatform agentPlatform;

    public ThinkingController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/thinking/ask")
    public Analysis ask(
            @RequestParam(value = "message", defaultValue = "一个笼子里有鸡和兔共 35 只，脚共 94 只，鸡和兔各有多少只？") String message) {
        return AgentInvocation.create(agentPlatform, Analysis.class).invoke(new UserInput(message));
    }
}
