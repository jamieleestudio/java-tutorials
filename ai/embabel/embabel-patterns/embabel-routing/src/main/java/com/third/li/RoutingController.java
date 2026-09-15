package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 路由接口。
 *
 * <p>试试不同的输入，观察命中的通道：
 * <pre>
 *   ?message=我买的商品坏了，想退款
 *   ?message=登录一直报 500 错误
 *   ?message=你们的营业时间是什么
 * </pre>
 */
@RestController
public class RoutingController {

    private final AgentPlatform agentPlatform;

    public RoutingController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/routing/ask")
    public Reply ask(
            @RequestParam(value = "message", defaultValue = "我买的商品坏了，想申请退款") String message) {
        return AgentInvocation.create(agentPlatform, Reply.class).invoke(new UserInput(message));
    }
}
