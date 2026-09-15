package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工具进阶接口。
 */
@RestController
public class ToolsAdvancedController {

    private final AgentPlatform agentPlatform;

    public ToolsAdvancedController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/tools-advanced/ask")
    public Reply ask(
            @RequestParam(value = "message", defaultValue = "帮我看看 A1001 这个订单的状态") String message) {
        return AgentInvocation.create(agentPlatform, Reply.class).invoke(new UserInput(message));
    }
}
