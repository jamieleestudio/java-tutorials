package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提示词工程接口。
 */
@RestController
public class PromptsController {

    private final AgentPlatform agentPlatform;

    public PromptsController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    /** 模板 + 人格 + 公共提示 */
    @GetMapping("/prompts/ask")
    public Reply ask(
            @RequestParam(value = "message", defaultValue = "单体应用什么时候该拆成微服务？") String message) {
        return AgentInvocation.create(agentPlatform, Reply.class).invoke(new UserInput(message));
    }

    /** @Provided 注入 Spring 组件 */
    @GetMapping("/prompts/provided")
    public FormattedReply provided(
            @RequestParam(value = "message", defaultValue = "什么是提示词模板？") String message) {
        return AgentInvocation.create(agentPlatform, FormattedReply.class).invoke(new UserInput(message));
    }
}
