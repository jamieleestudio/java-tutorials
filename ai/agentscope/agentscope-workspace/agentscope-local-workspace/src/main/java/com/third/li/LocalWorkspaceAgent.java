package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.filesystem.local.LocalFilesystem;
import io.agentscope.harness.agent.filesystem.spec.LocalFilesystemSpec;
import io.agentscope.harness.agent.tool.FilesystemTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 本地工作区（LocalFilesystem + FilesystemTool）。
 *
 * <p>AgentScope 的本地工作区通过 {@link LocalFilesystem} 实现：
 * <ul>
 *   <li>Agent 的文件操作限制在指定目录内</li>
 *   <li>支持 ls / read / write / edit / grep / glob</li>
 *   <li>通过 {@link LocalFilesystemSpec} 可配置 virtualMode / inheritEnv 等</li>
 * </ul>
 *
 * <p>两种配置方式：
 * <ol>
 *   <li>{@code .workspace(path)} — 使用默认 LocalFilesystem</li>
 *   <li>{@code .filesystem(LocalFilesystemSpec)} — 精细配置</li>
 * </ol>
 *
 * <p>本模块用方式 2 配置只读虚拟模式。
 */
@Component
public class LocalWorkspaceAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public LocalWorkspaceAgent(
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
                    var wsPath = Paths.get(".agentscope/workspace-local-workspace").toAbsolutePath();
                    wsPath.toFile().mkdirs();
                    LocalFilesystemSpec spec = new LocalFilesystemSpec()
                            .project(wsPath)
                            .projectWritable(true)
                            .executeTimeoutSeconds(10);
                    local = HarnessAgent.builder()
                            .name("local-workspace")
                            .sysPrompt("你是一个文件管理助手。你工作在 " + wsPath + " 目录。" +
                                    "你可以读写文件和列出目录内容。")
                            .model(model)
                            .filesystem(spec)
                            .workspace(wsPath)
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