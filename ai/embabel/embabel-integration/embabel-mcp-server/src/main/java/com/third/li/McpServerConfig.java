package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.servlet.http.HttpServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * 把 Embabel Agent 暴露为 **MCP server**（SSE 传输）。
 *
 * <p>为什么自己搭：官方 {@code embabel-agent-starter-mcp-server} 在当前依赖仓库里不可用，
 * 而 MCP SDK 的 **server 端**类（{@code mcp-core}）在 classpath 上，因此这里手工装配——
 * 也正好看清 MCP server 的三个组成部分：
 * <ol>
 *   <li><b>传输</b>：{@link HttpServletSseServerTransportProvider}（SSE + POST 消息端点），
 *       注册成 Servlet</li>
 *   <li><b>服务</b>：{@code McpServer.sync(transport)} 声明 serverInfo 与工具</li>
 *   <li><b>工具实现</b>：工具被调用时，转成一次 Embabel Agent 调用（{@link AgentInvocation}）</li>
 * </ol>
 *
 * <p>与 {@code embabel-mcp}（客户端）对称：那边是"我们用别人的工具"，这里是"别人用我们的能力"。
 */
@Configuration
public class McpServerConfig {

    private static final Logger log = LoggerFactory.getLogger(McpServerConfig.class);

    @Bean
    public HttpServletSseServerTransportProvider sseTransportProvider() {
        return HttpServletSseServerTransportProvider.builder()
                .sseEndpoint("/sse")
                .messageEndpoint("/mcp/message")
                .build();
    }

    @Bean(destroyMethod = "close")
    public McpSyncServer mcpServer(
            HttpServletSseServerTransportProvider transportProvider,
            AgentPlatform agentPlatform) {

        McpSchema.Tool askTool = McpSchema.Tool.builder()
                .name("ask_embabel")
                .description("向 Embabel Agent 提问并返回回答。适合技术概念解释、方案建议类问题。")
                .inputSchema(new McpSchema.JsonSchema(
                        "object",
                        Map.of("question", Map.of(
                                "type", "string",
                                "description", "要向 Agent 提出的问题")),
                        List.of("question"),
                        false,
                        null,
                        null))
                .build();

        McpSyncServer server = McpServer.sync(transportProvider)
                .serverInfo("embabel-mcp-server", "1.0.0")
                .instructions("把 Embabel Agent 的能力通过 MCP 暴露给任意 MCP 客户端")
                .tool(askTool, (exchange, arguments) -> {
                    String question = String.valueOf(arguments.getOrDefault("question", ""));
                    log.info("MCP tool 'ask_embabel' invoked with: {}", question);
                    try {
                        Reply reply = AgentInvocation.create(agentPlatform, Reply.class)
                                .invoke(new UserInput(question));
                        return McpSchema.CallToolResult.builder()
                                .addTextContent(reply.content())
                                .isError(false)
                                .build();
                    } catch (Exception e) {
                        log.warn("Agent invocation failed: {}", e.getMessage());
                        return McpSchema.CallToolResult.builder()
                                .addTextContent("Agent 调用失败：" + e.getMessage())
                                .isError(true)
                                .build();
                    }
                })
                .build();

        log.info("MCP server started with tool: ask_embabel");
        return server;
    }

    /** 把 SSE 传输注册为 Servlet：/sse（事件流）与 /mcp/message（JSON-RPC 消息） */
    @Bean
    public ServletRegistrationBean<HttpServlet> mcpServlet(
            HttpServletSseServerTransportProvider transportProvider) {
        return new ServletRegistrationBean<>(transportProvider, "/sse", "/mcp/message");
    }
}
