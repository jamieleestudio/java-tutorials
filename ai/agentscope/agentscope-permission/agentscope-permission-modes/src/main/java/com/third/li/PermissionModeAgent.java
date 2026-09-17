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
 * 权限模式（DEFAULT / ACCEPT_EDITS / EXPLORE / BYPASS / DONT_ASK）。
 *
 * <p>AgentScope 的权限系统有 5 种模式（{@link PermissionMode}）：
 * <ul>
 *   <li>{@code DEFAULT} — 标准模式，危险操作需确认</li>
 *   <li>{@code ACCEPT_EDITS} — 自动接受文件编辑，不确认</li>
 *   <li>{@code EXPLORE} — 只读模式，不允许任何修改操作</li>
 *   <li>{@code BYPASS} — 完全跳过权限检</li>
 *   <li>{@code DONT_ASK} — 不询问，按规则自动决策</li>
 * </ul>
 *
 * <p>两种设置方式：
 * <ol>
 *   <li><b>构造时</b>：{@link HarnessAgent.Builder#permissionContext(PermissionContextState)}</li>
 *   <li><b>运行时</b>：{@code agent.setPermissionMode(ctx, mode)}</li>
 * </ol>
 *
 * <p>本模块演示运行时切换模式——每个端点用不同模式调用同一个 Agent。
 */
@Component
public class PermissionModeAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public PermissionModeAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public String chat(String message, PermissionMode mode) {
        HarnessAgent a = agent();
        RuntimeContext ctx = runtimeContext();
        a.setPermissionMode(ctx, mode);
        return a.call(new UserMessage(message), ctx).block().getTextContent();
    }

    /** 查看当前模式。 */
    public String currentMode() {
        return agent().getPermissionMode("perm-demo", "alice").getValue();
    }

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    PermissionContextState permCtx = PermissionContextState.builder()
                            .mode(PermissionMode.DEFAULT)
                            .build();
                    local = HarnessAgent.builder()
                            .name("permission-modes")
                            .sysPrompt("你是一个文件管理助手。你可以读写文件和执行命令。" +
                                    "请根据当前权限模式行动。")
                            .model(model)
                            .permissionContext(permCtx)
                            .workspace(Paths.get(".agentscope/workspace-permission-modes"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("perm-demo").userId("alice").build();
    }
}