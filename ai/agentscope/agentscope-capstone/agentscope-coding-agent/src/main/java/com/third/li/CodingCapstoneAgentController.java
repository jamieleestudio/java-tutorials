package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class CodingCapstoneAgentController {

    private final CodingCapstoneAgent agent;

    public CodingCapstoneAgentController(CodingCapstoneAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/coding-agent/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "编码 Agent：shell+file+todo+plan mode 串起来") String message) {
        return agent.chat(message);
    }
}