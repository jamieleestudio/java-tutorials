package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.sandbox.snapshot.NoopSnapshotSpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 工作区快照（SandboxSnapshotSpec + NoopSandboxSnapshot）。
 *
 * <p>AgentScope 的快照系统允许保存/恢复工作区状态：
 * <ul>
 *   <li>{@code SandboxSnapshotSpec} — 快照策略接口</li>
 *   <li>{@code LocalSandboxSnapshot} — 本地文件快照</li>
 *   <li>{@code RemoteSandboxSnapshot} — 远程存储快照</li>
 *   <li>{@code NoopSandboxSnapshot} — 无操作（默认）</li>
 * </ul>
 *
 * <p>用途：Agent 执行任务后可以保存快照，下次从快照恢复，避免重复工作。
 * 本模块演示 Noop 快照（默认行为）。
 */
@Component
public class WorkspaceSnapshotAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public WorkspaceSnapshotAgent(
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

    /** 描述快照配置方式。 */
    public String describeSnapshots() {
        return """
                工作区快照配置：
                1. NoopSnapshotSpec — 不做快照（默认）
                2. LocalSnapshotSpec — 保存到本地 .tar.gz
                3. RemoteSnapshotSpec — 保存到远程存储

                使用方式：
                DockerFilesystemSpec spec = new DockerFilesystemSpec()
                    .image("openjdk:21-slim")
                    .snapshotSpec(new LocalSnapshotSpec(Paths.get("snapshots/")));
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
                    local = HarnessAgent.builder()
                            .name("workspace-snapshot")
                            .sysPrompt("你是一个助手。你的工作区支持快照功能。")
                            .model(model)
                            .workspace(Paths.get(".agentscope/workspace-snapshot"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("snapshot-demo").userId("alice").build();
    }
}