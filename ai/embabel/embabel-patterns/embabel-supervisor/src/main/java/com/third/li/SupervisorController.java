package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Supervisor 模式接口。
 *
 * <p>调用方只声明想要 {@link FinalAnswer}，具体由主管 LLM 决定先调哪个工人动作。
 * 日志里能看到 {@code Supervisor iteration N: ... curried tools} 与每轮的决策。
 */
@RestController
public class SupervisorController {

    private final AgentPlatform agentPlatform;

    public SupervisorController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/supervisor/ask")
    public FinalAnswer ask(
            @RequestParam(value = "message", defaultValue = "为什么 Agent 需要类型化的领域模型？") String message) {
        return AgentInvocation.create(agentPlatform, FinalAnswer.class).invoke(new UserInput(message));
    }
}
