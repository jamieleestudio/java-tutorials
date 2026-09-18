package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.model.ExecutionConfig;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * Programmatic DSL 模式（编程式构造，对照 Kotlin DSL）。
 *
 * <p>Embabel 有 Kotlin DSL（{@code agent(name) { promptedTransformer {...} }}），
 * AgentScope Java 没有——它用<b>纯 Java Builder 链</b>实现同等的声明式构造。
 *
 * <p>对照关系：
 * <table>
 *   <tr><th>Embabel Kotlin DSL</th><th>AgentScope Java Builder</th></tr>
 *   <tr><td>{@code agent(name, desc) {...}}</td><td>{@code HarnessAgent.builder().name().sysPrompt().build()}</td></tr>
 *   <tr><td>{@code transformation<I,O> {...}}</td><td>{@code agent.call(msgs, Class/JsonNode)}</td></tr>
 *   <tr><td>{@code goal(satisfiedBy=X)}</td><td>{@code maxIters} + 评估逻辑</td></tr>
 *   <tr><td>{@code flow { aggregate(...) }}</td><td>{@code Flux.merge(...).collectList()}</td></tr>
 * </table>
 *
 * <p>本模块演示：用 Builder 链式构造一个"带工具 + 重试 + 停止条件"的完整 Agent——
 * 全部在代码中声明，无外部配置文件。
 */
@Component
public class ProgrammaticDslAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public ProgrammaticDslAgent(
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

    /**
     * 用纯 Java Builder 声明式构造完整 Agent。
     *
     * <p>这一条链等价于 Embabel 的 Kotlin DSL 声明——
     * 名称、提示词、模型、工具、执行配置、停止条件一步到位。
     */
    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    Toolkit toolkit = new Toolkit();
                    toolkit.registerTool(new GreetingTool());
                    local = HarnessAgent.builder()                    // agent("dsl-agent") {
                            .name("dsl-agent")                          //   name
                            .description("纯 Java Builder 构造的 Agent")
                            .sysPrompt("你是一个助手。打招呼时用 greet 工具。") //   promptedTransformer
                            .model(model)                               //   llm
                            .toolkit(toolkit)                           //   tools
                            .toolExecutionConfig(ExecutionConfig.builder()
                                    .maxAttempts(2)
                                    .timeout(java.time.Duration.ofSeconds(10))
                                    .build())                           //   execution config
                            .maxIters(5)                                //   stop condition
                            .workspace(Paths.get(".agentscope/agentscope-programmatic-dsl"))
                            .build();                                   // }
                    agent = local;
                }
            }
        }
        return local;
    }

    /** 问候工具。 */
    public static class GreetingTool {
        @Tool(name = "greet", description = "返回一句问候语")
        public String greet(
                @ToolParam(name = "name", description = "问候对象") String name) {
            return "你好，" + name + "！很高兴见到你。";
        }
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("programmatic-dsl").userId("alice").build();
    }
}