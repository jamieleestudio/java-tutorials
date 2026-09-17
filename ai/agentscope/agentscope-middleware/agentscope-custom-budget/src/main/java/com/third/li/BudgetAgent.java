package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 带自定义预算熔断中间件的 Agent。
 *
 * <p>AgentScope Java 没有 Python 的 {@code BudgetControlMiddleware}，
 * 本模块通过 {@link BudgetControlMiddleware}（基于 {@code MiddlewareBase.onReasoning}）
 * 自行实现 token / 迭代次数双限。
 *
 * <p>提供两组对比：
 * <ul>
 *   <li>{@code /budget/ask} — 带预算（maxTokens=500, maxIters=3），长对话会被短路</li>
 *   <li>{@code /budget/unlimited} — 不带预算（对照组）</li>
 * </ul>
 */
@Component
public class BudgetAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent budgetedAgent;
    private volatile HarnessAgent unlimitedAgent;
    private final BudgetControlMiddleware budgetMiddleware = new BudgetControlMiddleware(500, 3);

    public BudgetAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    /** 带预算的 Agent：超限短路。 */
    public String chat(String message) {
        return budgetedAgent().call(new UserMessage(message), runtimeContext()).block().getTextContent();
    }

    /** 不带预算的 Agent（对照组）。 */
    public String chatUnlimited(String message) {
        return unlimitedAgent().call(new UserMessage(message), runtimeContext()).block().getTextContent();
    }

    /** 预算报告。 */
    public String report() {
        return budgetMiddleware.getReport().block();
    }

    private HarnessAgent budgetedAgent() {
        HarnessAgent local = budgetedAgent;
        if (local == null) {
            synchronized (this) {
                local = budgetedAgent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("custom-budget")
                            .sysPrompt("你是一个乐于助人的中文智能助手。请尽量详细回答。")
                            .model(model)
                            .middleware(budgetMiddleware)
                            .maxIters(5)
                            .workspace(java.nio.file.Paths.get(".agentscope/workspace-custom-budget"))
                            .build();
                    budgetedAgent = local;
                }
            }
        }
        return local;
    }

    private HarnessAgent unlimitedAgent() {
        HarnessAgent local = unlimitedAgent;
        if (local == null) {
            synchronized (this) {
                local = unlimitedAgent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("unlimited")
                            .sysPrompt("你是一个乐于助人的中文智能助手。")
                            .model(model)
                            .workspace(java.nio.file.Paths.get(".agentscope/workspace-custom-budget"))
                            .build();
                    unlimitedAgent = local;
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