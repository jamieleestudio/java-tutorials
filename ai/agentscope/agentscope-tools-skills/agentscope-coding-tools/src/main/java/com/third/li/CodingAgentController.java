package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class CodingAgentController {

    private final CodingAgent agent;

    public CodingAgentController(CodingAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/coding/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "编码工具：Shell 命令 + 文件读写 + Todo 列表") String message) {
        return agent.chat(message);
    }
}