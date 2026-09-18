package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 自主 Agent 接口。 */
@RestController
public class AutonomousAgentAgentController {

    private final AutonomousAgentAgent agent;

    public AutonomousAgentAgentController(AutonomousAgentAgent agent) {
        this.agent = agent;
    }

    /** 自主调用工具完成任务。 */
    @GetMapping("/patterns/autonomous/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "帮我计算 (3+5)*2 再查一下北京和上海的天气") String message) {
        return agent.chat(message);
    }
}