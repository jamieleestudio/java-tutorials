package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 工具链接口。 */
@RestController
public class ToolChainingAgentController {

    private final ToolChainingAgent agent;

    public ToolChainingAgentController(ToolChainingAgent agent) {
        this.agent = agent;
    }

    /** 链式：创建项目 → 添加任务 → 查看任务。 */
    @GetMapping("/patterns/tool-chaining/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "创建一个名为'网站重构'的项目，添加两个任务，然后列出任务") String message) {
        return agent.chat(message);
    }
}