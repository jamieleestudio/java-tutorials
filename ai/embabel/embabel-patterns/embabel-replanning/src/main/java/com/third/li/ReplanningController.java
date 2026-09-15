package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 动态重规划接口。
 *
 * <p>观察日志里的重规划与工具重试：第一次 {@code lookup_topic} 返回 FAILED → 触发重规划 →
 * 动作重跑 → 第二次调用成功。
 */
@RestController
public class ReplanningController {

    private final AgentPlatform agentPlatform;

    public ReplanningController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/replan/ask")
    public Reply ask(
            @RequestParam(value = "message", defaultValue = "帮我查一下 Embabel 的资料要点") String message) {
        return AgentInvocation.create(agentPlatform, Reply.class).invoke(new UserInput(message));
    }
}
