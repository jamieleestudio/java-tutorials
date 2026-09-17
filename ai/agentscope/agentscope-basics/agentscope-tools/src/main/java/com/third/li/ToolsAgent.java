package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 带工具的 Agent：把 {@link OrderQueryTools} 注册到 Toolkit，
 * 模型在 ReAct 循环中自主决定调用哪个工具。
 */
@Component
public class ToolsAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private final OrderQueryTools orderTools;
    private volatile HarnessAgent agent;

    public ToolsAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl,
            OrderQueryTools orderTools) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.orderTools = orderTools;
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
                    Toolkit toolkit = new Toolkit();
                    toolkit.registerTool(orderTools);

                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey)
                            .modelName(modelName)
                            .baseUrl(baseUrl)
                            .build();

                    local = HarnessAgent.builder()
                            .name("tools-assistant")
                            .sysPrompt("你是一个订单助手，可以使用工具查询订单、获取时间、计算折扣。请根据用户请求选择合适的工具。")
                            .model(model)
                            .toolkit(toolkit)
                            .workspace(java.nio.file.Paths.get(".agentscope/workspace-tools"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("tools-demo").userId("alice").build();
    }
}