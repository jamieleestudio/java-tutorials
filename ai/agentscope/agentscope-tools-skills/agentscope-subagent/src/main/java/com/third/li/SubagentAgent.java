package com.third.li;

import io.agentscope.core.agent.Agent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 子代理（SubAgent）Agent：通过 {@code HarnessAgent.Builder.subagentFactory(name, factory)}
 * 注册一个子 Agent 工厂，主 Agent 在 ReAct 循环中可"调用子 Agent"作为工具。
 *
 * <p>这是 AgentScope 的编排能力——主 Agent 把子 Agent 当成一个工具调用：
 * <ul>
 *   <li>{@code subagentFactory(String name, Function<String, Agent> factory)} ——
 *       注册一个按需创建子 Agent 的工厂，{@code name} 是暴露给主 Agent 的工具名</li>
 *   <li>主 Agent 决定何时调用子 Agent传什么消息，子 Agent 独立运行后返回结果</li>
 * </ul>
 *
 * <p>本例注册一个"翻译子 Agent"：主 Agent 收到翻译请求时，调用子 Agent 完成翻译。
 */
@Component
public class SubagentAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-subagent";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public SubagentAgent(
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

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("subagent-parent")
                            .sysPrompt("你是一个任务协调助手。遇到翻译任务时，调用 translate 子代理完成。")
                            .model(model)
                            .subagentFactory("translate", prompt -> buildTranslateSubAgent(prompt))
                            .workspace(Paths.get(WORKSPACE_DIR))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private Agent buildTranslateSubAgent(String prompt) {
        OpenAIChatModel model = OpenAIChatModel.builder()
                .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
        return HarnessAgent.builder()
                .name("translate-subagent")
                .sysPrompt("你是一个专业翻译。请将用户给定的文本翻译成目标语言，只输出译文。")
                .model(model)
                .workspace(Paths.get(WORKSPACE_DIR + "/translate"))
                .build();
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("subagent-demo").userId("alice").build();
    }
}