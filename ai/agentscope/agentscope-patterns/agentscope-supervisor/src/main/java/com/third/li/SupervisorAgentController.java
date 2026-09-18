package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 主管编排接口。 */
@RestController
public class SupervisorAgentController {

    private final SupervisorAgent agent;

    public SupervisorAgentController(SupervisorAgent agent) {
        this.agent = agent;
    }

    /** 主管协调研究员 + 写手完成报告。 */
    @GetMapping("/patterns/supervisor/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "写一篇关于智能体编排模式的介绍文章") String message) {
        return agent.chat(message);
    }
}