package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Prompt Chaining 接口。 */
@RestController
public class PromptChainingAgentController {

    private final PromptChainingAgent agent;

    public PromptChainingAgentController(PromptChainingAgent agent) {
        this.agent = agent;
    }

    /** 三步提示链：提炼 → 分析 → 总结（含关卡）。 */
    @GetMapping("/patterns/prompt-chain/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "分析远程办公对团队效率的影响") String message) {
        return agent.chain(message);
    }
}