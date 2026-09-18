package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 计划模式 Agent：通过 {@code HarnessAgent.Builder.enablePlanMode()} 开启计划模式——
 * Agent 先输出计划（写入 plan 文件），用户确认后再执行。
 *
 * <p>计划模式的关键 Builder 方法：
 * <ul>
 *   <li>{@code enablePlanMode()} —— 开启计划模式，注入 {@code PlanModeMiddleware}</li>
 *   <li>{@code planFileDirectory(String)} —— plan 文件存放目录</li>
 *   <li>{@code allowShellInPlanMode()} —— 计划阶段是否允许执行 shell（默认禁止）</li>
 * </ul>
 *
 * <p>运行时控制：{@link HarnessAgent#enterPlanMode(RuntimeContext)} / {@link HarnessAgent#exitPlanMode(RuntimeContext)} /
 * {@link HarnessAgent#isPlanModeActive(RuntimeContext)}。
 *
 * <p>计划阶段 Agent 只能读取和规划，不能修改文件或执行危险操作；
 * 用户确认后退出计划模式，Agent 才开始执行。
 */
@Component
public class PlanModeAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-plan-mode";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public PlanModeAgent(
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

    public String chatInPlanMode(String message) {
        RuntimeContext ctx = runtimeContext();
        agent().enterPlanMode(ctx);
        try {
            return agent().call(new UserMessage(message), ctx).block().getTextContent();
        } finally {
            agent().exitPlanMode(ctx);
        }
    }

    public boolean isPlanActive() {
        return agent().isPlanModeActive(runtimeContext());
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
                            .sysPrompt("你是一个规划型助手。先制定计划，确认后再执行。")
                            .model(model)
                            .enablePlanMode()
                            .planFileDirectory(".agentscope/plans")
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
                .sessionId("plan-demo").userId("alice").build();
    }
}