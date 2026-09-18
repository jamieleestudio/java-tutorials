package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.permission.PermissionBehavior;
import io.agentscope.core.permission.PermissionContextState;
import io.agentscope.core.permission.PermissionMode;
import io.agentscope.core.permission.PermissionRule;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 权限规则 Agent：用 {@link PermissionRule} 定义 allow / deny / ask 规则，
 * 装进 {@link PermissionContextState} 注入 Agent。
 *
 * <p>{@link PermissionRule} 是一个 record：{@code (toolName, ruleContent, behavior, source)}。
 * {@code ruleContent} 是自然语言描述的匹配条件（如"删除任意文件"），
 * {@link io.agentscope.core.permission.PermissionEngine} 会在运行时把工具调用与规则匹配，
 * 按行为（ALLOW/DENY/ASK）做决策。
 *
 * <p>本例定义：
 * <ul>
 *   <li>allow 规则：查询类工具直接放行</li>
 *   <li>deny 规则：删除文件工具一律拒绝</li>
 *   <li>ask 规则：写文件工具需用户确认</li>
 * </ul>
 */
@Component
public class PermissionRuleAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-permission-rules";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public PermissionRuleAgent(
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
                    PermissionRule allowQuery = new PermissionRule(
                            "*", "读取或查询类操作", PermissionBehavior.ALLOW, "demo");
                    PermissionRule denyDelete = new PermissionRule(
                            "ShellCommandTool", "执行 rm 或删除文件", PermissionBehavior.DENY, "demo");
                    PermissionRule askWrite = new PermissionRule(
                            "WriteFileTool", "写入或修改文件", PermissionBehavior.ASK, "demo");

                    PermissionContextState permCtx = PermissionContextState.builder()
                            .mode(PermissionMode.DEFAULT)
                            .addAllowRule("*", allowQuery)
                            .addDenyRule("ShellCommandTool", denyDelete)
                            .addAskRule("WriteFileTool", askWrite)
                            .build();

                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("permission-rules")
                            .sysPrompt("你是一个智能助手，可执行 shell 和文件工具。"
                                    + "系统会按权限规则自动放行/拒绝/弹确认。")
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
                .sessionId("perm-rule-demo").userId("alice").build();
    }
}