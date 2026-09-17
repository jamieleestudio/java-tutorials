package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.mcp.McpClientBuilder;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.time.Duration;

/**
 * MCP 客户端（McpClientBuilder SSE/stdio/HTTP）。
 *
 * <p>AgentScope 通过 {@link McpClientBuilder} 接入外部 MCP (Model Context Protocol) server，
 * 把 server 暴露的工具自动注册为 Agent 的工具。
 *
 * <p>三种传输方式：
 * <ul>
 *   <li>{@code stdioTransport(command, args)} — 启动子进程，通过 stdin/stdout 通信</li>
 *   <li>{@code sseTransport(url)} — Server-Sent Events，HTTP 长连接</li>
 *   <li>{@code streamableHttpTransport(url)} — 流式 HTTP</li>
 * </ul>
 *
 * <p>本模块演示 stdio 和 SSE 两种模式的<b>构建</b>方式。
 * 实际连接需要可用的 MCP server（如 npx @anthropic/mcp-server-filesystem）。
 */
@Component
public class McpAgent {

    private static final Logger log = LoggerFactory.getLogger(McpAgent.class);

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public McpAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public String chat(String message) {
        return agent().call(new UserMessage(message), runtimeContext()).block().getTextContent();
    }

    /**
     * 构建 stdio MCP 客户端（示例：npx filesystem server）。
     *
     * <p>返回构建描述字符串（实际初始化是异步的）。
     */
    public String describeStdioSetup() {
        return """
                Stdio MCP 客户端构建示例：
                McpClientBuilder.create("filesystem")
                    .stdioTransport("npx", "@anthropic/mcp-server-filesystem", "/tmp")
                    .timeout(Duration.ofSeconds(30))
                    .buildAsync()
                    .flatMap(client -> toolkit.registerMcpClient(client))
                    .subscribe();
                """;
    }

    /**
     * 构建 SSE MCP 客户端（示例：远程 server）。
     */
    public String describeSseSetup() {
        return """
                SSE MCP 客户端构建示例：
                McpClientBuilder.create("remote-tools")
                    .sseTransport("http://localhost:3001/sse")
                    .header("Authorization", "Bearer token123")
                    .timeout(Duration.ofSeconds(60))
                    .buildAsync()
                    .flatMap(client -> toolkit.registerMcpClient(client))
                    .subscribe();
                """;
    }

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    Toolkit toolkit = new Toolkit();
                    // MCP 客户端注册是异步的；这里展示构建模式。
                    // 实际使用时取消注释并配置真实的 MCP server：
                    // McpClientBuilder.create("filesystem")
                    //     .stdioTransport("npx", "@anthropic/mcp-server-filesystem")
                    //     .timeout(Duration.ofSeconds(30))
                    //     .buildAsync()
                    //     .flatMap(toolkit::registerMcpClient)
                    //     .subscribe(
                    //         ok -> log.info("[mcp] filesystem server 已连接"),
                    //         err -> log.warn("[mcp] 连接失败：{}", err.getMessage()));
                    local = HarnessAgent.builder()
                            .name("mcp-agent")
                            .sysPrompt("你是一个工具集成助手。你可以通过 MCP 协议调用外部工具。")
                            .model(model)
                            .toolkit(toolkit)
                            .workspace(Paths.get(".agentscope/workspace-mcp"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("mcp-demo").userId("alice").build();
    }
}