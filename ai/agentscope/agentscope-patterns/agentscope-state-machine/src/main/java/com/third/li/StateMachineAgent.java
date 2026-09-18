package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * State Machine 模式（状态机 / 阶段收敛工具）。
 *
 * <p>Agent 的执行过程被划分成<b>阶段（状态）</b>，不同阶段暴露不同工具能力——
 * 与 Embabel 的 {@code embabel-state-machine}（按状态收敛工具集 + 显式转移）同思路。
 *
 * <p>AgentScope 的实现方式：用 <b>PlanMode（计划模式）</b> 实现"分析/执行"两阶段：
 * <ul>
 *   <li><b>分析状态</b>（PLAN）：{@code enterPlanMode()}，只读分析，写入工具被禁用</li>
 *   <li><b>执行状态</b>（ACT）：{@code exitPlanMode()}，恢复写入和执行能力</li>
 * </ul>
 *
 * <p>本模块演示显式的状态转移：用户通过端点手动切换 PLAN ↔ ACT，
 * 每个状态下 Agent 的能力不同。
 */
@Component
public class StateMachineAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public StateMachineAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    /** 在指定状态下与 Agent 对话。 */
    public String chat(String message, boolean planMode) {
        RuntimeContext ctx = runtimeContext();
        if (planMode) {
            agent().enterPlanMode(ctx);
        } else {
            agent().exitPlanMode(ctx);
        }
        return agent().call(new UserMessage(message), ctx).block().getTextContent();
    }

    /** 查看当前状态。 */
    public String status() {
        boolean inPlan = agent().isPlanModeActive(runtimeContext());
        return inPlan
                ? "状态：PLAN（分析阶段，只读，写入工具禁用）"
                : "状态：ACT（执行阶段，写入工具可用）";
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
                            .name("state-machine")
                            .sysPrompt("你是项目助手。你有两种工作状态：计划模式（分析）和" +
                                    "执行模式（操作）。根据当前模式行动。")
                            .model(model)
                            .enablePlanMode()
                            .planFileDirectory("plans")
                            .workspace(Paths.get(".agentscope/agentscope-state-machine"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("state-machine").userId("alice").build();
    }
}