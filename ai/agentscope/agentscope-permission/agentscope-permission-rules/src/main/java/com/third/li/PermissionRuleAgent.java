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
import java.util.List;

/**
 * 权限规则（PermissionRule + PermissionEngine）。
 *
 * <p>AgentScope 的权限规则是<b>细粒度</b>的——按工具名定义 allow/deny/ask 规则：
 * <ul>
 *   <li>{@code allow} — 允许执行，不询问</li>
 *   <li>{@code deny} — 拒绝执行</li>
 *   <li>{@code ask} — 需要户确认</li>
 *   <li>{@code passthrough} — 传递给下一条规则</li>
 * </ul>
 *
 * <p>{@link PermissionRule} 是 record：(toolName, ruleContent, behavior, source)。
 * 规则按工具名分组（Map<String, List<PermissionRule>>）。
 *
 * <p>本模块演示：
 * <ul>
 *   <li>允许 readFile / listFiles（read-only 工具）</li>
 *   <li>拒绝 deleteFile（危险操作）</li>
 *   <li>询问 createFile（需要确认）</li>
 * </ul>
 */
@Component
public class PermissionRuleAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public PermissionRuleAgent(
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

                    PermissionRule allowRead = new PermissionRule(
                            "readFile", "**", PermissionBehavior.ALLOW, "tutorial");
                    PermissionRule allowList = new PermissionRule(
                            "listFiles", "**", PermissionBehavior.ALLOW, "tutorial");
                    PermissionRule denyDelete = new PermissionRule(
                            "deleteFile", "**", PermissionBehavior.DENY, "tutorial");
                    PermissionRule askCreate = new PermissionRule(
                            "createFile", "**", PermissionBehavior.ASK, "tutorial");

                    PermissionContextState permCtx = PermissionContextState.builder()
                            .mode(PermissionMode.DONT_ASK)
                            .addAllowRule("readFile", allowRead)
                            .addAllowRule("listFiles", allowList)
                            .addDenyRule("deleteFile", denyDelete)
                            .addAskRule("createFile", askCreate)
                            .build();

                    local = HarnessAgent.builder()
                            .name("permission-rules")
                            .sysPrompt("你是一个文件管理助手。你有 readFile, listFiles, " +
                                    "createFile, deleteFile 四个工具。请根据用户请求选择工具。")
                            .model(model)
                            .permissionContext(permCtx)
                            .workspace(Paths.get(".agentscope/workspace-permission-rules"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("rules-demo").userId("alice").build();
    }
}