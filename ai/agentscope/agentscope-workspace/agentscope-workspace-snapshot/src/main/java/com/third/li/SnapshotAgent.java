package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.sandbox.impl.docker.DockerFilesystemSpec;
import io.agentscope.harness.agent.sandbox.snapshot.LocalSnapshotSpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 工作区快照 Agent：给 Docker 沙箱配置 {@link LocalSnapshotSpec}，
 * 让 Agent 每次运行的工作区状态可以快照保存 / 恢复。
 *
 * <p>{@link LocalSnapshotSpec} 实现了 {@code SandboxSnapshotSpec}，
 * 通过 {@code DockerFilesystemSpec.snapshotSpec(spec)} 注入。
 * 运行时 Agent 的沙箱状态会持久化到 {@code snapshotBasePath} 目录，
 * 可在下次运行时从快照恢复（{@code SandboxSnapshot.restore()}）。
 *
 * <p>典型用途：
 * <ul>
 *   <li>调试时回滚到某个中间状态</li>
 *   <li>长任务中断后从断点恢复</li>
 *   <li>复现 Agent 的执行环境</li>
 * </ul>
 */
@Component
public class SnapshotAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-snapshot";
    private static final String SNAPSHOT_DIR = ".agentscope/snapshots";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private final String dockerImage;
    private volatile HarnessAgent agent;

    public SnapshotAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl,
            @Value("${agentscope.docker.image:openjdk:21-slim}") String dockerImage) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.dockerImage = dockerImage;
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
                    LocalSnapshotSpec snapshotSpec = new LocalSnapshotSpec(
                            Paths.get(SNAPSHOT_DIR).toAbsolutePath());
                    DockerFilesystemSpec sandboxSpec = new DockerFilesystemSpec()
                            .image(dockerImage)
                            .workspaceRoot("/workspace")
                            .snapshotSpec(snapshotSpec);

                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("workspace-snapshot")
                            .sysPrompt("你是一个沙箱助手，工作区状态可快照保存与恢复。")
                            .model(model)
                            .filesystem(sandboxSpec)
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
                .sessionId("snapshot-demo").userId("alice").build();
    }
}