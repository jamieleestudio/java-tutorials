package com.third.li;

import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.gateway.HarnessGateway;
import io.agentscope.harness.agent.gateway.channel.ChannelConfig;
import io.agentscope.harness.agent.gateway.channel.chatui.ChatUiChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.List;

/**
 * ChatUI 渠道 Agent：用内置的 {@link ChatUiChannel} 提供浏览器可点击的 Web UI 渠道。
 *
 * <p>{@link ChatUiChannel} 是 AgentScope 内置渠道，特点：
 * <ul>
 *   <li>{@code ChatUiChannel.create(config)} —— 创建实例，绑定到 {@link HarnessGateway}</li>
 *   <li>{@code channel.send(message)} —— 发消息给 Agent，返回回复</li>
 *   <li>{@code channel.pollOutbound()} —— 轮询出站消息（供前端拉取展示）</li>
 *   <li>{@code channel.sendStream(message)} —— 流式输出</li>
 * </ul>
 *
 * <p>本例把 ChatUiChannel 注册到网关，绑定主 Agent，
 * {@link #chat} 通过 channel.send 调用 Agent。
 */
@Component
public class ChatUiAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-chatui";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessGateway gateway;
    private volatile ChatUiChannel chatUiChannel;

    public ChatUiAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public String chat(String message) {
        Msg reply = chatUiChannel().send(message).block();
        return reply != null ? reply.getTextContent() : "（无回复）";
    }

    private ChatUiChannel chatUiChannel() {
        ChatUiChannel local = chatUiChannel;
        if (local == null) {
            synchronized (this) {
                local = chatUiChannel;
                if (local == null) {
                    HarnessGateway gw = HarnessGateway.create();
                    gw.bindMainAgent(mainAgent());
                    local = ChatUiChannel.create(ChannelConfig.of(ChatUiChannel.CHANNEL_ID, "chatui-agent"));
                    gw.channelManager().register(local);
                    gw.channelManager().initAll(gw);
                    gateway = gw;
                    chatUiChannel = local;
                }
            }
        }
        return local;
    }

    private HarnessAgent mainAgent() {
        OpenAIChatModel model = OpenAIChatModel.builder()
                .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
        return HarnessAgent.builder()
                .name("chatui-agent")
                .sysPrompt("你是一个 ChatUI 助手，通过内置 Web UI 渠道与用户交互。")
                .model(model)
                .workspace(Paths.get(WORKSPACE_DIR))
                .build();
    }
}