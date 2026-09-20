package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * MCP 客户端：接入外部 MCP server（如本仓库 8518 端口的 saa-mcp-server）。
 *
 * <p>{@code spring-ai-starter-mcp-client-webflux} 自动装配 {@link ToolCallbackProvider}，
 * 把 yml 里 {@code spring.ai.mcp.client.sse.connections.<name>.url} 指向的
 * MCP server 的工具全部拉取下来，再注入 ChatClient，模型即可像调用本地 @Tool 一样
 * 调用远程 MCP 工具。
 *
 * <p>启动顺序：先启动 spring-ai-alibaba-mcp-server（8518），再启动本模块，
 * 否则 MCP 连接失败（应用仍会起，只是拿不到工具）。
 */
@RestController
public class McpClientController {

    private final ChatClient chatClient;
    private final ToolCallbackProvider mcpToolProvider;

    public McpClientController(ChatClient.Builder chatClientBuilder,
                               ToolCallbackProvider mcpToolProvider) {
        this.mcpToolProvider = mcpToolProvider;
        this.chatClient = chatClientBuilder
                .defaultToolCallbacks(mcpToolProvider)
                .build();
    }

    /** 列出从 MCP server 拉到的工具。 */
    @GetMapping("/mcp/tools")
    public List<String> tools() {
        return java.util.Arrays.stream(mcpToolProvider.getToolCallbacks())
                .map(ToolCallback::getToolDefinition)
                .map(def -> def.name() + " — " + def.description())
                .toList();
    }

    /** 让模型自主调用 MCP server 的工具。 */
    @GetMapping("/mcp/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "查一下杭州今天的天气，并给出穿衣建议") String message) {
        return chatClient.prompt(message).call().content();
    }
}
