package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 多模型接口。
 */
@RestController
public class MultiModelController {

    private final AgentPlatform agentPlatform;

    public MultiModelController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    /** 按角色路由：fast 列要点、deep 写终稿 */
    @GetMapping("/multi-model/ask")
    public Answer ask(
            @RequestParam(value = "message", defaultValue = "如何为一个客服场景设计 Agent？") String message) {
        return AgentInvocation.create(agentPlatform, Answer.class).invoke(new UserInput(message));
    }

    /** 模型回退：按候选顺序挑第一个可用的 */
    @GetMapping("/multi-model/fallback")
    public FallbackAnswer fallback(
            @RequestParam(value = "message", defaultValue = "一句话说明什么是模型回退") String message) {
        return AgentInvocation.create(agentPlatform, FallbackAnswer.class).invoke(new UserInput(message));
    }
}
