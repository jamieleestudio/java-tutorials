package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.permission.PermissionBehavior;
import io.agentscope.core.permission.PermissionContextState;
import io.agentscope.core.permission.PermissionMode;
import io.agentscope.core.permission.PermissionRule;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 人工确认（HITL: Human-in-the-Loop）。
 *
 * <p>AgentScope 的 HITL 通过权限系统的 {@code ASK} 行为实现：
 * <ul>
 *   <li>定义 {@code ASK} 规则——特定工具调用时触发确认</li>
 *   <li>{@link PermissionMode#DEFAULT} 模式下，ASK 规则会暂停 Agent 执行</li>
 *   <li>用户确认后继续，拒绝则终止</li>
 * </ul>
 *
 * <p>本模块定义了一个"危险操作"工具（sendEmail），给它设置 ASK 规则。
 * 在 DEFAULT 模式下，Agent 调用 sendEmail 时会触发确认流程。
 *
 * <p>与 Embabel 的差异：Embabel 的 HITL 通过 @Action(canRerun=true) +
 * AwaitingDecider 实现；AgentScope 通过权限系统的 ASK behavior 实现——
 * 更统一，不需要额外的状态管理。
 */
@Component
public class HitlConfirmAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public HitlConfirmAgent(
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

                    PermissionRule askEmail = new PermissionRule(
                            "sendEmail", "**", PermissionBehavior.ASK, "security-policy");
                    PermissionContextState permCtx = PermissionContextState.builder()
                            .mode(PermissionMode.DEFAULT)
                            .addAskRule("sendEmail", askEmail)
                            .build();

                    Toolkit toolkit = new Toolkit();
                    toolkit.registerTool(new EmailTool());

                    local = HarnessAgent.builder()
                            .name("hitl-confirm")
                            .sysPrompt("你是一个邮件助手。当用户让你发邮件时，" +
                                    "使用 sendEmail 工具发送。发邮件前会需要用户确认。")
                            .model(model)
                            .toolkit(toolkit)
                            .permissionContext(permCtx)
                            .workspace(Paths.get(".agentscope/workspace-hitl-confirm"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("hitl-demo").userId("alice").build();
    }

    /** 邮件工具：被标记为需要确认。 */
    public static class EmailTool {
        @Tool(name = "sendEmail", description = "发送邮件到指定地址")
        public String sendEmail(
                @ToolParam(name = "to", description = "收件人邮箱") String to,
                @ToolParam(name = "subject", description = "邮件主题") String subject,
                @ToolParam(name = "body", description = "邮件内容") String body) {
            return "邮件已发送至 " + to + "，主题：" + subject;
        }
    }
}