package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 状态机接口。
 */
@RestController
public class StateMachineController {

    private final AgentPlatform agentPlatform;

    public StateMachineController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/state-machine/process")
    public OrderResult process(
            @RequestParam(value = "order",
                    defaultValue = "处理订单 A1001：客户张三，金额 ¥299，要求今天发货") String order) {
        return AgentInvocation.create(agentPlatform, OrderResult.class).invoke(new UserInput(order));
    }
}
