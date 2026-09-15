package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 子 Agent 协作接口。
 */
@RestController
public class SubagentController {

    private final AgentPlatform agentPlatform;

    public SubagentController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/subagent/translate")
    public ChatReply translate(
            @RequestParam(value = "message", defaultValue = "把这句话翻译成英文：今天天气真好，我们去公园吧。") String message) {
        return AgentInvocation.create(agentPlatform, ChatReply.class).invoke(new UserInput(message));
    }
}
