package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 动态重规划接口。 */
@RestController
public class ReplanningAgentController {

    private final ReplanningAgent agent;

    public ReplanningAgentController(ReplanningAgent agent) {
        this.agent = agent;
    }

    /** 工具失败时自动重规划换路径。 */
    @GetMapping("/patterns/replanning/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "查询用户 10086 的信息") String message) {
        return agent.chat(message);
    }
}