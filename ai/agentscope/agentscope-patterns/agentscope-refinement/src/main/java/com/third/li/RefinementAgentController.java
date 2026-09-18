package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 自评迭代接口。 */
@RestController
public class RefinementAgentController {

    private final RefinementAgent agent;

    public RefinementAgentController(RefinementAgent agent) {
        this.agent = agent;
    }

    /** 生成 → 评估 → 改进，直到评分达标。 */
    @GetMapping("/patterns/refinement/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "写一段关于 Agent 框架演进的技术文案") String message) {
        return agent.refine(message);
    }
}