package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 网关接口。 */
@RestController
public class GatewayAgentController {

    private final GatewayAgent agent;

    public GatewayAgentController(GatewayAgent agent) {
        this.agent = agent;
    }

    /** 直接调用（绕过 Gateway）。 */
    @GetMapping("/gateway/direct")
    public String direct(
            @RequestParam(value = "message", defaultValue = "你好") String message) {
        return agent.chat(message);
    }

    /** 通过 Gateway 调用（模拟 Channel 入站）。 */
    @GetMapping("/gateway/routed")
    public String routed(
            @RequestParam(value = "message", defaultValue = "你好") String message) {
        return agent.gatewayChat(message);
    }
}