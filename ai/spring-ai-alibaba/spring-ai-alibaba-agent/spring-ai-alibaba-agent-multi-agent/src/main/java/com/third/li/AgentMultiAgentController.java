package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 多 Agent 协作接口：GET /agent/multi/ask
 */
@RestController
public class AgentMultiAgentController {

    private final AgentMultiAgentService agentService;

    public AgentMultiAgentController(AgentMultiAgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping("/agent/multi/ask")
    public String ask(
            @RequestParam(value = "message",
                    defaultValue = "帮我总结一下：Spring AI Alibaba 把 Spring AI 的模型抽象延伸到了图编排与智能体框架，让 Java 开发者可以用声明式的方式构建复杂 Agent 应用。")
            String message) throws Exception {
        return agentService.ask(message);
    }
}
