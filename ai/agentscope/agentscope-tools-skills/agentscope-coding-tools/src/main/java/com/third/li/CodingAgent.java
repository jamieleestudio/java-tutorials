package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.filesystem.local.LocalFilesystemWithShell;
import io.agentscope.harness.agent.tool.FilesystemTool;
import io.agentscope.harness.agent.tool.ShellExecuteTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 编码工具（ShellExecuteTool + FilesystemTool）。
 *
 * <p>AgentScope 内置两个编码核心工具：
 * <ul>
 *   <li>{@link ShellExecuteTool} — 在沙箱/本地执行 shell 命令</li>
 *   <li>{@link FilesystemTool} — 读/写/编辑/grep/glob/ls 文件操作</li>
 * </ul>
 *
 * <p>关键：{@code ShellExecuteTool} 构造时需要 {@code AbstractSandboxFilesystem}，
 * 这里用 {@link LocalFilesystemWithShell}（本地 + shell 能力的文件系统）。
 * {@code FilesystemTool} 构造时需要 {@code AbstractFilesystem}。
 *
 * <p>与 Embabel 的差异：Embabel 没有内置 shell/file 工具——
 * 它是"声明式"的（@Action + tool），工具是用户定义的。
 * AgentScope 内置了完整的 coding 工具链。
 */
@Component
public class CodingAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public CodingAgent(
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

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    var workspacePath = Paths.get(".agentscope/workspace-coding-tools");
                    workspacePath.toFile().mkdirs();
                    LocalFilesystemWithShell fs = new LocalFilesystemWithShell(workspacePath);
                    ShellExecuteTool shellTool = new ShellExecuteTool(fs);
                    FilesystemTool fileTool = new FilesystemTool(fs);
                    var toolkit = new io.agentscope.core.tool.Toolkit();
                    toolkit.registerTool(shellTool);
                    toolkit.registerTool(fileTool);
                    local = HarnessAgent.builder()
                            .name("coding-tools")
                            .sysPrompt("你是一个编码助手。你可以执行 shell 命令和读写文件。" +
                                    "当用户让你创建项目时，先创建文件，再执行构建命令。")
                            .model(model)
                            .toolkit(toolkit)
                            .workspace(workspacePath)
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