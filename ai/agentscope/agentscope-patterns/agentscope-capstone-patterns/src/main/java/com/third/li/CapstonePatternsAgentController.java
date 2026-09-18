package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 模式综合接口。 */
@RestController
public class CapstonePatternsAgentController {

    private final CapstonePatternsAgent agent;

    public CapstonePatternsAgentController(CapstonePatternsAgent agent) {
        this.agent = agent;
    }

    /** 端到端：自主开发 + 工具链 + 子 Agent 审查。 */
    @GetMapping("/patterns/capstone/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "创建一个项目，写一个 Hello World Java 文件并运行") String message) {
        return agent.chat(message);
    }
}