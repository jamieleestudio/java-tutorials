package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.filesystem.remote.store.InMemoryStore;
import io.agentscope.harness.agent.filesystem.spec.RemoteFilesystemSpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 远程文件系统 Agent：用 {@link RemoteFilesystemSpec} 把工作区后端换成远程存储。
 *
 * <p>{@link RemoteFilesystemSpec} 把文件操作路由到一个 {@code BaseStore}（键值存储抽象），
 * 而不是本地磁盘。本例用内存版 {@link InMemoryStore} 演示；生产环境可换成
 * Redis / S3 / 数据库 backed 的 store。
 *
 * <p>关键点：
 * <ul>
 *   <li>{@code new RemoteFilesystemSpec(baseStore)} —— 指定后端 store</li>
 *   <li>{@code addSharedPrefix(String)} —— 声明跨会话共享的路径前缀</li>
 *   <li>{@code anonymousUserId(String)} —— 匿名用户标识</li>
 * </ul>
 *
 * <p>通过 {@code HarnessAgent.Builder.filesystem(RemoteFilesystemSpec)} 注入。
 */
@Component
public class RemoteFsAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-remote-filesystem";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public RemoteFsAgent(
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
                    InMemoryStore store = new InMemoryStore();
                    RemoteFilesystemSpec remoteSpec = new RemoteFilesystemSpec(store)
                            .addSharedPrefix("shared/")
                            .anonymousUserId("demo-user");

                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("remote-filesystem")
                            .sysPrompt("你是一个远程工作区助手，文件存储在远程后端。")
                            .model(model)
                            .filesystem(remoteSpec)
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
                .sessionId("remote-fs-demo").userId("alice").build();
    }
}