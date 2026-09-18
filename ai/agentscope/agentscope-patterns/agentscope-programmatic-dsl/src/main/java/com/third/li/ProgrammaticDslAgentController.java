package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 编程式 DSL 接口。 */
@RestController
public class ProgrammaticDslAgentController {

    private final ProgrammaticDslAgent agent;

    public ProgrammaticDslAgentController(ProgrammaticDslAgent agent) {
        this.agent = agent;
    }

    /** Builder 链构造的 Agent。 */
    @GetMapping("/patterns/dsl/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "和小明打个招呼") String message) {
        return agent.chat(message);
    }
}