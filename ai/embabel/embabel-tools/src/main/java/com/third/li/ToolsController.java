package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工具调用接口。
 */
@RestController
public class ToolsController {

    private final AgentPlatform agentPlatform;

    public ToolsController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/tools/ask")
    public ChatReply ask(
            @RequestParam(value = "message", defaultValue = "北京天气怎么样？另外帮我算一下 123 * 456") String message) {
        return AgentInvocation.create(agentPlatform, ChatReply.class).invoke(new UserInput(message));
    }
}
