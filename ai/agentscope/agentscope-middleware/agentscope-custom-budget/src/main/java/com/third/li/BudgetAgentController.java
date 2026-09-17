package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 预算熔断接口。 */
@RestController
public class BudgetAgentController {

    private final BudgetAgent agent;

    public BudgetAgentController(BudgetAgent agent) {
        this.agent = agent;
    }

    /** 带预算：超 token/迭代上限时短路终止。 */
    @GetMapping("/budget/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "请详细介绍 Spring Boot 的自动注入原理，尽量详细") String message) {
        return agent.chat(message);
    }

    /** 不带预算（对照组）。 */
    @GetMapping("/budget/unlimited")
    public String unlimited(
            @RequestParam(value = "message", defaultValue = "请详细介绍 Spring Boot 的自动注入原理") String message) {
        return agent.chatUnlimited(message);
    }

    /** 查看预算报告。 */
    @GetMapping("/budget/report")
    public String report() {
        return agent.report();
    }
}