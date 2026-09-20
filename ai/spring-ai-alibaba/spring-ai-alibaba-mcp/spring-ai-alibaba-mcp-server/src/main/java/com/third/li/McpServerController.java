package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MCP 服务端说明接口：GET /mcp/info
 */
@RestController
public class McpServerController {

    @GetMapping("/mcp/info")
    public String info() {
        return """
                SAA MCP Server 已启动。
                SSE 端点: /sse
                已注册工具: weather(city), order(orderId)
                配合 spring-ai-alibaba-mcp-client（8517）体验：GET http://localhost:8517/mcp/ask
                """;
    }
}
