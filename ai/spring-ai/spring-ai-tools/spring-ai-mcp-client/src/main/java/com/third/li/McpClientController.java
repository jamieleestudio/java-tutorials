package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * MCP 客户端（接入外部 MCP server）。
 *
 * <p>Spring AI 2.0 通过 <code>spring-ai-starter-mcp-client</code> 接入 MCP server：
 * <ul>
 *   <li>依赖：{@code spring-ai-starter-mcp-client-webflux}（SSE 传输）</li>
 *   <li>yml 配置 {@code spring.ai.mcp.client.servers.<name>}</li>
 *   <li>自动装配 {@code ToolCallbackProvider}，把 MCP server 的工具注册给模型</li>
 * </ul>
 *
 * <p>本模块演示：把 MCP server 的工具通过 {@link ToolCallbackProvider} 注入 ChatClient。
 * 需要启动一个 MCP server（如 npx @anthropic/mcp-server-filesystem）。
 */
@RestController
public class McpClientController {

    private final ChatClient chatClient;

    public McpClientController(ChatClient.Builder chatClientBuilder,
            ToolCallbackProvider mcpToolProvider) {
        this.chatClient = chatClientBuilder
                .defaultToolCallbacks(mcpToolProvider)
                .build();
    }

    /** 模型自主调用 MCP server 暴露的工具。 */
    @GetMapping("/ai/mcp")
    public String mcp(
            @RequestParam(value = "message", defaultValue = "列出 /tmp 目录下的文件") String message) {
        return chatClient.prompt(message).call().content();
    }

    /** 查看 MCP 工具接入说明。 */
    @GetMapping("/ai/mcp/tools")
    public String tools() {
        return "MCP 工具通过 ToolCallbackProvider 自动注入 ChatClient。\n"
                + "配置：spring.ai.mcp.client.servers.<name>.url=<sse-url>";
    }
}
