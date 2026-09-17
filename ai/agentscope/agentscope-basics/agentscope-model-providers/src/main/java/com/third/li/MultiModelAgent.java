package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 多模型 Agent：演示三种模型使用方式。
 *
 * <p>AgentScope 的模型管理有两个层次：
 * <ol>
 *   <li><b>直接构建</b>：{@code OpenAIChatModel.builder().apiKey().modelName().baseUrl().build()}
 *       ——手动构造，适合需要精细控制的场景</li>
 *   <li><b>按名解析</b>：{@code HarnessAgent.builder().model("deepseek-v4-flash")}
 *       ——通过 {@link io.agentscope.core.model.ModelRegistry} 按名解析，
 *       适合配置驱动的场景（模型名从 yml/环境变量来）</li>
 * </ol>
 *
 * <p>本模块同时演示 <b>fallback（模型降级）</b>：
 * {@code HarnessAgent.Builder.fallbackModel(model)}——当主模型调用失败时自动切到备用模型。
 * 这在 Embabel 里是 {@code multi-model} 模块的"role→model 映射"的等价物，
 * 但 AgentScope 的 fallback 是**调用级自动降级**，不是按角色选模型。
 */
@Component
public class MultiModelAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private final String fallbackName;
    private final String fallbackBaseUrl;
    private volatile HarnessAgent agent;

    public MultiModelAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl,
            @Value("${agentscope.model.fallback-name:deepseek-v4-pro}") String fallbackName,
            @Value("${agentscope.model.fallback-base-url:https://api.deepseek.com}") String fallbackBaseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.fallbackName = fallbackName;
        this.fallbackBaseUrl = fallbackBaseUrl;
    }

    /** 用主模型回答。 */
    public String chatWithPrimary(String message) {
        return agent().call(new UserMessage(message), runtimeContext()).block().getTextContent();
    }

    /** 构建一个指定模型的 Agent（演示按需切换）。 */
    public String chatWithModel(String message, String modelLabel) {
        HarnessAgent oneShot = buildAgent(modelLabel);
        return oneShot.call(new UserMessage(message), runtimeContext()).block().getTextContent();
    }

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    local = buildAgentWithFallback();
                    agent = local;
                }
            }
        }
        return local;
    }

    /** 带 fallback 的 Agent：主模型失败时自动切备用。 */
    private HarnessAgent buildAgentWithFallback() {
        OpenAIChatModel primary = OpenAIChatModel.builder()
                .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
        OpenAIChatModel fallback = OpenAIChatModel.builder()
                .apiKey(apiKey).modelName(fallbackName).baseUrl(fallbackBaseUrl).build();
        return HarnessAgent.builder()
                .name("multi-model")
                .sysPrompt("你是一个乐于助人的中文智能助手。")
                .model(primary)
                .fallbackModel(fallback)
                .maxRetries(1)
                .workspace(java.nio.file.Paths.get(".agentscope/workspace-models"))
                .build();
    }

    /** 按标签构建单模型 Agent。 */
    private HarnessAgent buildAgent(String label) {
        OpenAIChatModel model;
        if ("pro".equalsIgnoreCase(label)) {
            model = OpenAIChatModel.builder()
                    .apiKey(apiKey).modelName(fallbackName).baseUrl(fallbackBaseUrl).build();
        } else {
            model = OpenAIChatModel.builder()
                    .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
        }
        return HarnessAgent.builder()
                .name("oneshot-" + label)
                .sysPrompt("你是一个乐于助人的中文智能助手。")
                .model(model)
                .workspace(java.nio.file.Paths.get(".agentscope/workspace-models"))
                .build();
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("model-demo").userId("alice").build();
    }
}