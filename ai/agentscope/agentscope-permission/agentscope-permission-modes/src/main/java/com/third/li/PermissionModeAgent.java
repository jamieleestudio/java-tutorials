package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.permission.PermissionContextState;
import io.agentscope.core.permission.PermissionMode;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 权限模式 Agent：演示 AgentScope 的三种权限模式——
 * {@link PermissionMode#BYPASS}（不确认）、{@link PermissionMode#DONT_ASK}（不弹确认，按规则放行）、
 * {@link PermissionMode#DEFAULT}（默认，危险操作弹确认）。
 *
 * <p>权限通过 {@code HarnessAgent.Builder.permissionContext(PermissionContextState)} 注入，
 * 运行时可用 {@link HarnessAgent#setPermissionMode(RuntimeContext, PermissionMode)} 动态切换。
 *
 * <p>本例默认用 {@link PermissionMode#DEFAULT}，并提供 {@link #chatWithMode(String, PermissionMode)}
 * 按需切换模式，便于对比三种模式下的工具确认行为。
 */
@Component
public class PermissionModeAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-permission-modes";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public PermissionModeAgent(
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

    public String chatWithMode(String message, PermissionMode mode) {
        RuntimeContext ctx = runtimeContext();
        agent().setPermissionMode(ctx, mode);
        return agent().call(new UserMessage(message), ctx).block().getTextContent();
    }

    public String currentMode() {
        return agent().getPermissionMode(runtimeContext().getSessionId(), runtimeContext().getUserId()).getValue();
    }

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    PermissionContextState permCtx = PermissionContextState.builder()
                            .mode(PermissionMode.DEFAULT)
                            .build();
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("permission-modes")
                            .sysPrompt("你是一个智能助手，可执行工具。根据权限模式决定是否需要用户确认。")
                            .model(model)
                            .permissionContext(permCtx)
                            .stopOnReject(true)
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
                .sessionId("perm-mode-demo").userId("alice").build();
    }
}