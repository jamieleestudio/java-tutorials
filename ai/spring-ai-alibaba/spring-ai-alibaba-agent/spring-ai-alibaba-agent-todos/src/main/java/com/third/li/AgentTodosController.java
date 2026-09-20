package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 任务清单接口：GET /agent/todos/ask
 */
@RestController
public class AgentTodosController {

    private final AgentTodosService service;

    public AgentTodosController(AgentTodosService service) {
        this.service = service;
    }

    @GetMapping("/agent/todos/ask")
    public String ask(
            @RequestParam(value = "message",
                    defaultValue = "策划一场 20 分钟的 Spring AI Alibaba Graph 主题分享，拆成不超过 3 步并逐步执行")
            String message) throws Exception {
        return service.ask(message);
    }
}
