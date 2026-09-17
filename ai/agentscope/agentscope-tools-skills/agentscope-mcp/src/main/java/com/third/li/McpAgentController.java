package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class McpAgentController {

    private final McpAgent agent;

    public McpAgentController(McpAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/mcp/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "MCP 客户端：接入外部 MCP server 获取工具") String message) {
        return agent.chat(message);
    }
}