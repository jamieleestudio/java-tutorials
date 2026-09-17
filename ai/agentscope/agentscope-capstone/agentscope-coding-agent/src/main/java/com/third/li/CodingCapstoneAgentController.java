package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 编码 Agent 接口。 */
@RestController
public class CodingCapstoneAgentController {

    private final CodingCapstoneAgent agent;

    public CodingCapstoneAgentController(CodingCapstoneAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/capstone/coding")
    public String ask(
            @RequestParam(value = "message", defaultValue = "创建一个 Hello World Java 项目并编译运行") String message) {
        return agent.chat(message);
    }
}