package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openaiofficial.OpenAIResponsesChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * OpenAI Responses API（AgentScope 2.0.4 新增模块 agentscope-extensions-model-openai-official）。
 *
 * <p>{@link OpenAIResponsesChatModel} 走 OpenAI 官方 SDK 的 Responses 端点
 * （/v1/responses，区别于传统 Chat Completions），支持原生结构化输出
 * （strictJsonSchema）、官方推理模型行为等。与 Chat Completions 的差异由模块内部处理，
 * 对 {@code HarnessAgent} 透明 —— 换一个 {@code model(...)} 即可。
 *
 * <p>注意：DeepSeek 等 Chat Completions 兼容端点不支持 Responses API，
 * 本模块需要真实的 OPENAI_API_KEY（应用可启动，调用时才需要）。
 */
@Component
public class ResponsesApiAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-responses-api";

    private final String modelName;
    private final String apiKey;
    private volatile HarnessAgent agent;

    public ResponsesApiAgent(
            @Value("${agentscope.responses.model:gpt-4.1-mini}") String modelName,
            @Value("${OPENAI_API_KEY:}") String apiKey) {
        this.modelName = modelName;
        this.apiKey = apiKey;
    }

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    OpenAIResponsesChatModel model = OpenAIResponsesChatModel.builder()
                            .apiKey(apiKey)
                            .modelName(modelName)
                            .build();
                    local = HarnessAgent.builder()
                            .name("responses-api")
                            .sysPrompt("你是一个乐于助人的中文智能助手。")
                            .model(model)
                            .workspace(Paths.get(WORKSPACE_DIR))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    public String chat(String message) {
        return agent().call(new UserMessage(message),
                        RuntimeContext.builder().sessionId("responses-demo").userId("alice").build())
                .block().getTextContent();
    }
}
