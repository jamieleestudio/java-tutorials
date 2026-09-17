package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 结构化输出 Agent：用 {@code agent.call(msg, Class)} 让模型直接返回强类型对象。
 *
 * <p>与 Embabel 的对照：
 * <ul>
 *   <li>Embabel: {@code ai.creating(X.class).fromPrompt(prompt)} → 返回 X</li>
 *   <li>AgentScope: {@code agent.call(msg, X.class).block().getStructuredData(X.class)} → 返回 X</li>
 * </ul>
 * 两者都是"告诉模型目标类型、框架自动生成 schema + 解析 JSON"的思路。
 */
@Component
public class StructuredOutputAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public StructuredOutputAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public ProductAnalysis analyze(String product) {
        Msg result = agent().call(
                        java.util.List.of(new UserMessage("请分析以下产品，给出结构化的分析报告：" + product)),
                        ProductAnalysis.class,
                        runtimeContext()).block();
        return result != null ? result.getStructuredData(ProductAnalysis.class) : null;
    }

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey)
                            .modelName(modelName)
                            .baseUrl(baseUrl)
                            .build();
                    local = HarnessAgent.builder()
                            .name("analyst")
                            .sysPrompt("你是一个产品分析师。请严格按照要求的结构输出分析报告。")
                            .model(model)
                            .workspace(java.nio.file.Paths.get(".agentscope/workspace-structured"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("structured-demo").userId("alice").build();
    }
}