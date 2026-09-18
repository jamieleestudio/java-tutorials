package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 路由接口。 */
@RestController
public class RoutingAgentController {

    private final RoutingAgent agent;

    public RoutingAgentController(RoutingAgent agent) {
        this.agent = agent;
    }

    /** 分类并路由到专用 Agent。 */
    @GetMapping("/patterns/routing/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "帮我看看如何优化 Java 应用的内存") String message) {
        return agent.route(message);
    }
}