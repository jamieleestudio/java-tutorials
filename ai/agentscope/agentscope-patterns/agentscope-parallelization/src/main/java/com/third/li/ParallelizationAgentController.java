package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 并行化接口。 */
@RestController
public class ParallelizationAgentController {

    private final ParallelizationAgent agent;

    public ParallelizationAgentController(ParallelizationAgent agent) {
        this.agent = agent;
    }

    /** Sectioning：多 Agent 并行分析不同维度。 */
    @GetMapping("/patterns/parallel/ask")
    public String sectioning(
            @RequestParam(value = "message", defaultValue = "评估建设一个新的电商平台") String message) {
        return agent.sectioning(message);
    }

    /** Voting：多 Agent 独立回答后投票。 */
    @GetMapping("/patterns/parallel/vote")
    public String voting(
            @RequestParam(value = "message", defaultValue = "远程办公会成为未来主流工作方式吗") String message) {
        return agent.voting(message);
    }
}