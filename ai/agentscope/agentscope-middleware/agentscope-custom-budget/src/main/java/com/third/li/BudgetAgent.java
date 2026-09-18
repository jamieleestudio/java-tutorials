package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 自定义预算熔断 Agent：注入 {@link BudgetGuardMiddleware}，
 * 累计 LLM 调用的 token 用量，超过预算时短路返回"预算已耗尽"。
 *
 * <p>AgentScope Java 版没有内置预算中间件（Python 版有 {@code BudgetMiddleware}），
 * 这正好演示 {@link io.agentscope.core.middleware.MiddlewareBase} 洋葱模型的**短路能力**：
 * 在 {@code onAgent} 钩子检查预算，超限时不调用 {@code next}，直接返回结果事件。
 *
 * <p>token 用量来自 {@link io.agentscope.core.event.ModelCallEndEvent#getUsage()}。
 *
 * <p>对照组 {@link #chatWithoutBudget} 不带预算中间件，可对比长对话下两者的差异。
 */
@Component
public class BudgetAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-custom-budget";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private final BudgetGuardMiddleware budgetGuard = new BudgetGuardMiddleware(5000);
    private volatile HarnessAgent agent;
    private volatile HarnessAgent plainAgent;

    public BudgetAgent(
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

    public String chatWithoutBudget(String message) {
        return plainAgent().call(new UserMessage(message), runtimeContext()).block().getTextContent();
    }

    public int usedTokens() {
        return budgetGuard.usedTokens();
    }

    public int budget() {
        return budgetGuard.budget();
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
                            .name("custom-budget")
                            .sysPrompt("你是一个乐于助人的中文智能助手。")
                            .model(model)
                            .middleware(budgetGuard)
                            .workspace(Paths.get(WORKSPACE_DIR))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private HarnessAgent plainAgent() {
        HarnessAgent local = plainAgent;
        if (local == null) {
            synchronized (this) {
                local = plainAgent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("custom-budget-plain")
                            .sysPrompt("你是一个乐于助人的中文智能助手。")
                            .model(model)
                            .workspace(Paths.get(WORKSPACE_DIR))
                            .build();
                    plainAgent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("budget-demo").userId("alice").build();
    }
}