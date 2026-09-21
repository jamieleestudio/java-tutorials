package com.third.li;

import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 主管模式接口：GET /patterns/supervisor?request=… */
@RestController
public class PatternSupervisorController {

    private final SupervisorAgent supervisor;

    public PatternSupervisorController(SupervisorAgent supervisor) {
        this.supervisor = supervisor;
    }

    @GetMapping("/patterns/supervisor")
    public String run(
            @RequestParam(defaultValue = "从 Mario 账户取出 200 元，并把这笔钱换算成日元") String request) {
        return supervisor.invoke(request);
    }
}
