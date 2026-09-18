package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 辩论接口。 */
@RestController
public class DebateAgentController {

    private final DebateAgent agent;

    public DebateAgentController(DebateAgent agent) {
        this.agent = agent;
    }

    /** 正反方辩论 + 裁判裁定。 */
    @GetMapping("/patterns/debate/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "远程办公应该成为企业的主流工作方式") String message) {
        return agent.debate(message);
    }
}