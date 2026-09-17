package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.sandbox.impl.docker.DockerFilesystemSpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * Docker 沙箱 Agent：用 {@link DockerFilesystemSpec}（一种 {@code SandboxFilesystemSpec}）
 * 把 Agent 的工具执行隔离到 Docker 容器里。
 *
 * <p>Docker 沙箱提供强隔离：Agent 执行的 shell 命令和文件操作都在容器内，
 * 不影响宿主机。{@link DockerFilesystemSpec} 关键参数：
 * <ul>
 *   <li>{@code image(String)} —— 容器镜像</li>
 *   <li>{@code workspaceRoot(String)} —— 容内工作区路径</li>
 *   <li>{@code memorySizeBytes / cpuCount} —— 资源限制</li>
 *   <li>{@code network(String)} —— 网络策略（可设为 none 断网）</li>
 * </ul>
 *
 * <p>通过 {@code HarnessAgent.Builder.filesystem(SandboxFilesystemSpec)} 注入。
 * 运行时需宿主机安装 Docker。
 */
@Component
public class DockerSandboxAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-docker-sandbox";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private final String dockerImage;
    private volatile HarnessAgent agent;

    public DockerSandboxAgent(
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
                    DockerFilesystemSpec sandboxSpec = new DockerFilesystemSpec()
                            .image(dockerImage)
                            .workspaceRoot("/workspace")
                            .memorySizeBytes(512L * 1024 * 1024)
                            .cpuCount(1L)
                            .network("none");

                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("docker-sandbox")
                            .sysPrompt("你是一个沙箱助手，所有命令在 Docker 容器内隔离执行。")
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
                .sessionId("docker-demo").userId("alice").build();
    }
}