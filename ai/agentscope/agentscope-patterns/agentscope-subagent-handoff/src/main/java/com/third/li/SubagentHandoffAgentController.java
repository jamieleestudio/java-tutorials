package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 子 Agent 委派接口。 */
@RestController
public class SubagentHandoffAgentController {

    private final SubagentHandoffAgent agent;

    public SubagentHandoffAgentController(SubagentHandoffAgent agent) {
        this.agent = agent;
    }

    /** 主 Agent 把翻译任务整体转交给翻译子 Agent。 */
    @GetMapping("/patterns/subagent/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "请翻译：The quick brown fox jumps over the lazy dog") String message) {
        return agent.chat(message);
    }
}