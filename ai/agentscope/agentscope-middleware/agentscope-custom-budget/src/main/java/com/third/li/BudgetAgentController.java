package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class BudgetAgentController {

    private final BudgetAgent agent;

    public BudgetAgentController(BudgetAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/budget/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "自定义预算中间件：在 onReasoning 钩子里检查 token 用量，超限则短路") String message) {
        return agent.chat(message);
    }
}