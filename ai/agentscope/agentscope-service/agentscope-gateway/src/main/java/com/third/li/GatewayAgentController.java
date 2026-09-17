package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class GatewayAgentController {

    private final GatewayAgent agent;

    public GatewayAgentController(GatewayAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/gateway/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "网关：多渠道路由，把消息分发给 Agent") String message) {
        return agent.chat(message);
    }
}