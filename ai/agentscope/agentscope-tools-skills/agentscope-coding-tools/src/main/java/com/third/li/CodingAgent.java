package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.builtin.TodoTools;
import io.agentscope.core.tool.coding.ShellCommandTool;
import io.agentscope.core.tool.file.ReadFileTool;
import io.agentscope.core.tool.file.WriteFileTool;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.Set;

/**
 * 编码工具 Agent：把 {@link ShellCommandTool}、{@link ReadFileTool}、
 * {@link WriteFileTool}、{@link TodoTools} 注册到同一个 {@link Toolkit}，
 * 模型在 ReAct 循环中可自主决定执行 shell 令、读写文件、维护 TODO 列表。
 *
 * <p>这组工具是 Claude Code 式编码 Agent 的最小工具集：
 * <ul>
 *   <li>{@link ShellCommandTool} —— 执行 shell 命令（默认只允许安全命令白名单）</li>
 *   <li>{@link ReadFileTool} / {@link WriteFileTool} —— 文件读写，限制在 workspace 目录内</li>
 *   <li>{@link TodoTools} —— TODO 列表管理，状态写入 {@code AgentState} 以跨轮次保持</li>
 * </ul>
 *
 * <p>与 {@code agentscope-tools} 模块的区别：那边用 {@code @Tool} 注解声明业务工具，
 * 这里直接使用 AgentScope 内置的编码工具类（它们实现了 {@code AgentTool} 接口，
 * 通过 {@link Toolkit#registerAgentTool} 注册）。
 */
@Component
public class CodingAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-coding-tools";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public CodingAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
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
                    toolkit.registerAgentTool(new ShellCommandTool(
                            WORKSPACE_DIR,
                            Set.of("ls", "cat", "echo", "pwd", "grep", "find", "wc", "head", "tail", "mkdir"),
                            cmd -> true));
                    toolkit.registerTool(new ReadFileTool(WORKSPACE_DIR));
                    toolkit.registerTool(new WriteFileTool(WORKSPACE_DIR));
                    toolkit.registerTool(new TodoTools());

                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("coding-tools")
                            .sysPrompt("你是一个编码助手，可以执行 shell 命令、读写文件、管理 TODO 列表。"
                                    + "请根据用户需求自主选择工具完成任务，操作范围限制在工作区目录内。")
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
                .sessionId("coding-demo").userId("alice").build();
    }
}