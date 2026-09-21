package com.third.li;

import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 主管模式接口：GET /ai/agentic/supervisor?request=… */
@RestController
public class AgenticSupervisorController {

    private final SupervisorAgent supervisor;

    public AgenticSupervisorController(SupervisorAgent supervisor) {
        this.supervisor = supervisor;
    }

    @GetMapping("/ai/agentic/supervisor")
    public String run(
            @RequestParam(defaultValue = "从 Mario 账户取出 200 元，并把这笔钱换算成日元") String request) {
        return supervisor.invoke(request);
    }
}