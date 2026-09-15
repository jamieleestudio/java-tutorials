package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 自主 Agent 接口。
 *
 * <p>观察日志里的 tool loop：模型会先调用工具、拿到错误后重试，最终给出回答。
 */
@RestController
public class AutonomousController {

    private final AgentPlatform agentPlatform;

    public AutonomousController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/autonomous/ask")
    public Answer ask(
            @RequestParam(value = "message",
                    defaultValue = "先查一下内部资料：引入 Agent 框架要注意什么？再算一下 1234 * 56 等于多少") String message) {
        return AgentInvocation.create(agentPlatform, Answer.class).invoke(new UserInput(message));
    }
}
