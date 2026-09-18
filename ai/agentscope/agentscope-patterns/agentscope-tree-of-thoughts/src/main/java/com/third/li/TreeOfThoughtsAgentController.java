package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 思维树接口。 */
@RestController
public class TreeOfThoughtsAgentController {

    private final TreeOfThoughtsAgent agent;

    public TreeOfThoughtsAgentController(TreeOfThoughtsAgent agent) {
        this.agent = agent;
    }

    /** 多分支探索 + 评分剪枝 + 最优深化。 */
    @GetMapping("/patterns/tot/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "如何降低企业软件开发的成本") String message) {
        return agent.solve(message);
    }
}