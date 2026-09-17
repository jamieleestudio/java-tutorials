package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.filesystem.spec.LocalFilesystemSpec;
import io.agentscope.harness.agent.workspace.LocalFsMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 本地工作区 Agent：用 {@link LocalFilesystemSpec} 配置本地文件系统工作区。
 *
 * <p>{@link LocalFilesystemSpec} 的关键参数：
 * <ul>
 *   <li>{@code project(Path)} —— 工作区根目录</li>
 *   <li>{@code mode(LocalFsMode)} —— 文件系统模式（读/写/虚拟）</li>
 *   <li>{@code projectWritable(boolean)} —— 项目目录是否可写</li>
 *   <li>{@code executeTimeoutSeconds(int)} —— shell 执行超时</li>
 * </ul>
 *
 * <p>通过 {@code HarnessAgent.Builder.filesystem(LocalFilesystemSpec)} 注入，
 * Agent 的文件工具（ReadFileTool/WriteFileTool/ShellCommandTool）会限制在该工作区内。
 */
@Component
public class LocalWorkspaceAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-local";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public LocalWorkspaceAgent(
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
                    LocalFilesystemSpec fsSpec = new LocalFilesystemSpec()
                            .project(Paths.get(WORKSPACE_DIR).toAbsolutePath())
                            .projectWritable(true)
                            .mode(LocalFsMode.SANDBOXED)
                            .executeTimeoutSeconds(30)
                            .maxOutputBytes(1 << 20);

                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("local-workspace")
                            .sysPrompt("你是一个工作区助手，可在本地工作区目录内读写文件、执行命令。")
                            .model(model)
                            .filesystem(fsSpec)
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
                .sessionId("local-ws-demo").userId("alice").build();
    }
}