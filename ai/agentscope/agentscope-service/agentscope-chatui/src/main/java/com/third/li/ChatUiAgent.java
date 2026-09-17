package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.gateway.HarnessGateway;
import io.agentscope.harness.agent.gateway.channel.ChannelConfig;
import io.agentscope.harness.agent.gateway.channel.InboundMessage;
import io.agentscope.harness.agent.gateway.channel.Peer;
import io.agentscope.harness.agent.gateway.channel.chatui.ChatUiChannel;
import io.agentscope.harness.agent.gateway.channel.chatui.OutboundEnvelope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.List;

/**
 * ChatUI Channel（ChatUiChannel）。
 *
 * <p>AgentScope 的 {@link ChatUiChannel} 是一个内置的聊天 UI 渠道：
 * <ul>
 *   <li>接收用户消息（inbound）→ 路由到 Agent</li>
 *   <li>Agent 回复（outbound）→ 排队等用户轮询</li>
 * </ul>
 *
 * <p>工作流：
 * <ol>
 *   <li>{@code channel.send(text)} — 发送消息给 Agent</li>
 *   <li>{@code channel.pollOutbound()} — 轮询回复队列</li>
 * </ol>
 *
 * <p>适合构建 Web 聊天界面。本模块演示 send + poll 模式。
 */
@Component
public class ChatUiAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;
    private volatile ChatUiChannel channel;

    public ChatUiAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    /** 通过 ChatUI Channel 发送消息。 */
    public String send(String message) {
        return channel().send(message).block().getTextContent();
    }

    /** 轮询出站消息（Agent 的回复）。 */
    public String pollOutbound() {
        List<OutboundEnvelope> envelopes = channel().pollOutbound();
        if (envelopes.isEmpty()) {
            return "(无新消息)";
        }
        StringBuilder sb = new StringBuilder();
        for (OutboundEnvelope env : envelopes) {
            sb.append(env.toString()).append("\n");
        }
        return sb.toString();
    }

    private ChatUiChannel channel() {
        ChatUiChannel local = channel;
        if (local == null) {
            synchronized (this) {
                local = channel;
                if (local == null) {
                    local = ChatUiChannel.create();
                    HarnessGateway gateway = HarnessGateway.create();
                    gateway.bindMainAgent(agent());
                    local.init(gateway);
                    local.start();
                    channel = local;
                }
            }
        }
        return local;
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
                            .name("chatui-agent")
                            .sysPrompt("你是一个聊天助手。通过 ChatUI Channel 与用户交互。")
                            .model(model)
                            .workspace(Paths.get(".agentscope/workspace-chatui"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }
}