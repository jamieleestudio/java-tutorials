package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.filesystem.remote.RemoteFilesystem;
import io.agentscope.harness.agent.filesystem.remote.store.InMemoryStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 远程文件系统（RemoteFilesystem + BaseStore）。
 *
 * <p>AgentScope 的远程文件系统通过 {@link RemoteFilesystem} 实现，
 * 文件存储在 {@code BaseStore}（如 InMemoryStore / RedisStore / S3Store）中，
 * 而不是本地磁盘。
 *
 * <p>用途：
 * <ul>
 *   <li>多 Agent 共享文件（分布式场景）</li>
 *   <li>远程 Agent（容器化）访问持久化存储</li>
 *   <li>试隔离（InMemoryStore）</li>
 * </ul>
 *
 * <p>本模块用 {@link InMemoryStore} 演示（生产用 RedisStore 等）。
 */
@Component
public class RemoteFilesystemAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public RemoteFilesystemAgent(
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
                    InMemoryStore store = new InMemoryStore();
                    RemoteFilesystem remoteFs = new RemoteFilesystem(store);
                    local = HarnessAgent.builder()
                            .name("remote-filesystem")
                            .sysPrompt("你是一个文件管理助手。你的文件存储在远程存储中。" +
                                    "你可以读写文件和列出目录。")
                            .model(model)
                            .abstractFilesystem(remoteFs)
                            .workspace(Paths.get(".agentscope/workspace-remote-fs"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("remote-fs-demo").userId("alice").build();
    }
}