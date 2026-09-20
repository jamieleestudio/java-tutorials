package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agent 工具调用接口：GET /agent/tools/ask
 */
@RestController
public class AgentToolsController {

    private final AgentToolsService agentService;

    public AgentToolsController(AgentToolsService agentService) {
        this.agentService = agentService;
    }

    @GetMapping("/agent/tools/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "现在几点了？杭州天气怎么样？") String message)
            throws Exception {
        return agentService.ask(message);
    }
}
