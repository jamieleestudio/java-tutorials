package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 决策服务接口：GET /reasoning/decisions?message=… */
@RestController
public class DecisionServiceController {

    private final AgentPlatform agentPlatform;

    public DecisionServiceController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    /** 分类 + 组合决策（是/否 + 单选 + 评级）一次完成。 */
    @GetMapping("/reasoning/decisions")
    public DecisionAgent.TicketReport decide(
            @RequestParam(defaultValue = "上周买的耳机有电流声，我要退货退款，今天必须给我处理！")
            String message) {
        return AgentInvocation.create(agentPlatform, DecisionAgent.TicketReport.class)
                .invoke(new UserInput(message));
    }
}
