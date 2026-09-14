package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 触发多步规划的接口。
 *
 * <p>{@link AgentInvocation} 只需声明最终目标类型 {@link Article}，
 * 中间的 {@code research -> outline -> write} 由规划器自动完成。
 */
@RestController
public class PlanningController {

    private final AgentPlatform agentPlatform;

    public PlanningController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/plan/generate")
    public Article generate(@RequestParam(value = "topic", defaultValue = "微服务架构") String topic) {
        return AgentInvocation.create(agentPlatform, Article.class).invoke(new UserInput(topic));
    }
}
