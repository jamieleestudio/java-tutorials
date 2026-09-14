package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 聊天接口。通过 Embabel 的 {@link AgentPlatform} 与 {@link AgentInvocation}
 * 以强类型方式运行 ChatAgent，并按目标类型 ChatReply 取回结果。
 */
@RestController
public class ChatController {

    private final AgentPlatform agentPlatform;

    public ChatController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/ai/generate")
    public String generate(@RequestParam(value = "message", defaultValue = "讲个笑话") String message) {
        AgentInvocation<ChatReply> invocation = AgentInvocation.create(agentPlatform, ChatReply.class);
        return invocation.invoke(new UserInput(message)).content();
    }

}
