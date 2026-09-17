package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class CuratorAgentController {

    private final CuratorAgent agent;

    public CuratorAgentController(CuratorAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/curator/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "技能策展：自动评估技能使用情况并晋升为持久技能") String message) {
        return agent.chat(message);
    }
}