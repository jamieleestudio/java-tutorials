package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 渐进式工具接口。 */
@RestController
public class ToolsAdvancedAgentController {

    private final ToolsAdvancedAgent agent;

    public ToolsAdvancedAgentController(ToolsAdvancedAgent agent) {
        this.agent = agent;
    }

    /** 使用自省 + 安全工具完成任务。 */
    @GetMapping("/patterns/tools-advanced/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "查询文档库里关于 Agent 的内容") String message) {
        return agent.chat(message);
    }
}