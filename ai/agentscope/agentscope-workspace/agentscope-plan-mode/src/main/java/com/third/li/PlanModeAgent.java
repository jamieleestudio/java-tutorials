package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 计划模式（enterPlanMode / exitPlanMode / isPlanModeActive）。
 *
 * <p>AgentScope 的计划模式让 Agent 先<b>分析</b>再<b>执行</b>：
 * <ul>
 *   <li>{@code enterPlanMode(ctx)} — 进入计划模式，Agent 只分析不执行</li>
 *   <li>{@code exitPlanMode(ctx)} — 退出计划模式，开始执行</li>
 *   <li>{@code isPlanModeActive(ctx)} — 检查是否在计划模式</li>
 * </ul>
 *
 * <p>计划模式下，Agent 的 shell/写入工具被禁用（只读分析，
 * 退出后恢复全部能力。
 *
 * <p>通过 {@link HarnessAgent.Builder#enablePlanMode()} 启用。
 * 还可配置 {@code .planFileDirectory("plans")} 指定计划文件存储目录。
 *
 * <p>与 Embabel 的差异：Embabel 的计划-执行分离通过 Process 的 goal 机制实现；
 * AgentScope 用模式切换——更灵活，可以在对话中动态切换。
 */
@Component
public class PlanModeAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public PlanModeAgent(
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

    /** 进入计划模式。 */
    public String enterPlanMode() {
        RuntimeContext ctx = runtimeContext();
        agent().enterPlanMode(ctx);
        return "已进入计划模式（只读分析）";
    }

    /** 退出计划模式。 */
    public String exitPlanMode() {
        RuntimeContext ctx = runtimeContext();
        agent().exitPlanMode(ctx);
        return "已退出计划模式（恢复执行能力）";
    }

    /** 检查是否在计划模式。 */
    public String checkPlanMode() {
        boolean active = agent().isPlanModeActive(runtimeContext());
        return active ? "当前在计划模式" : "当前不在计划模式";
    }

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("plan-mode")
                            .sysPrompt("你是一个项目助手。在计划模式下，你分析需求并制定计划。" +
                                    "在执行模式下，你执行操作。")
                            .model(model)
                            .enablePlanMode()
                            .planFileDirectory("plans")
                            .workspace(Paths.get(".agentscope/workspace-plan-mode"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("plan-demo").userId("alice").build();
    }
}