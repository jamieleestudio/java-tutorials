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
 * Docker 沙箱（DockerFilesystemSpec + DockerSandbox）。
 *
 * <p>AgentScope 的 Docker 沙箱把 Agent 的文件操作和 shell 命令限制在
 * Docker 容器内，提供隔离的执行环境：
 * <ul>
 *   <li>文件操作（read/write/edit）在容器内执行</li>
 *   <li>Shell 命令在容器内运行</li>
 *   <li>可配置镜像、内存、CPU、端口、网络</li>
 * </ul>
 *
 * <p>通过 {@link DockerFilesystemSpec} 配置：
 * <pre>
 * DockerFilesystemSpec spec = new DockerFilesystemSpec()
 *     .image("openjdk:21-slim")
 *     .workspaceRoot("/workspace")
 *     .memorySizeBytes(512L * 1024 * 1024)
 *     .cpuCount(2L)
 *     .network("none");
 * </pre>
 *
 * <p>需要本地安装 Docker。本模块构建 spec 但不启动（避免依赖 Docker 环境）。
 * 实际使用时通过 {@code .filesystem(spec)} 注入。
 */
@Component
public class DockerSandboxAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public DockerSandboxAgent(
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

    /** 构建 Docker 沙箱 spec（不启动，仅展示配置）。 */
    public String describeSandbox() {
        DockerFilesystemSpec spec = new DockerFilesystemSpec()
                .image("openjdk:21-slim")
                .workspaceRoot("/workspace")
                .memorySizeBytes(512L * 1024 * 1024)
                .cpuCount(2L)
                .network("none");
        return """
                Docker 沙箱配置：
                镜像：openjdk:21-slim
                工作区：/workspace
                内存限制：512MB
                CPU：2 核
                网络：none（无网络访问）
                使用方式：.filesystem(spec)
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
                    // 实际使用 Docker 沙箱时取消注释：
                    // DockerFilesystemSpec spec = new DockerFilesystemSpec()
                    //         .image("openjdk:21-slim")
                    //         .workspaceRoot("/workspace")
                    //         .memorySizeBytes(512L * 1024 * 1024);
                    local = HarnessAgent.builder()
                            .name("docker-sandbox")
                            .sysPrompt("你是一个代码执行助手。你在 Docker 沙箱中运行，" +
                                    "可以安全地执行代码和命令。")
                            .model(model)
                            // .filesystem(spec)  // 需要 Docker 环境
                            .workspace(Paths.get(".agentscope/workspace-docker-sandbox"))
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