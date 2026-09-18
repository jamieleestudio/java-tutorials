package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.mcp.McpClientBuilder;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * MCP 客户端 Agent：用 {@link McpClientBuilder} 构建一个 SSE 传输的 MCP 客户端，
 * 连到外部 MCP Server，把它暴露的工具注册进 {@link Toolkit}。
 *
 * <p>{@link McpClientBuilder} 支持三种传输：
 * <ul>
 *   <li>{@code stdioTransport} —— 启动子进程，用 stdin/stdout 通信</li>
 *   <li>{@code sseTransport} —— Server-Sent Events，连远程 HTTP 端点</li>
 *   <li>{@code streamableHttpTransport} —— 可流式 HTTP 传输</li>
 * </ul>
 *
 * <p>本例用 SSE 连接 {@code mcpServerUrl}（默认本地 8765）。
 * MCP 客户端初始化是异步的（{@link McpClientWrapper#initialize()} 返回 Mono），
 * 这里在 agent 构建时注册到 Toolkit，运行时按需初始化。
 */
@Component
public class McpAgent {

    private static final Logger log = LoggerFactory.getLogger(McpAgent.class);
    private static final String WORKSPACE_DIR = ".agentscope/workspace-mcp";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private final String mcpServerUrl;
    private volatile HarnessAgent agent;

    public McpAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl,
            @Value("${agentscope.mcp.server-url:http://localhost:8765/sse}") String mcpServerUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.mcpServerUrl = mcpServerUrl;
    }

    public String chat(String message) {
        return agent().call(new UserMessage(message), runtimeContext()).block().getTextContent();
    }

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    Toolkit toolkit = new Toolkit();
                    McpClientWrapper mcpClient = McpClientBuilder.create("demo-mcp")
                            .sseTransport(mcpServerUrl)
                            .timeout(java.time.Duration.ofSeconds(10))
                            .buildSync();
                    toolkit.registerMcpClient(mcpClient).block();
                    log.info("[mcp] 已注册 MCP 客户端 demo-mcp，server={}", mcpServerUrl);

                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("mcp-agent")
                            .sysPrompt("你是一个智能助手，可通过 MCP 协议调用外部工具。")
                            .model(model)
                            .toolkit(toolkit)
                            .workspace(Paths.get(WORKSPACE_DIR))
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