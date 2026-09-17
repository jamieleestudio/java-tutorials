package com.third.li;

import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.gateway.ChannelManager;
import io.agentscope.harness.agent.gateway.HarnessGateway;
import io.agentscope.harness.agent.gateway.channel.Channel;
import io.agentscope.harness.agent.gateway.channel.ChannelConfig;
import io.agentscope.harness.agent.gateway.channel.chatui.ChatUiChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 多渠道路由（ChannelManager + Channel）。
 *
 * <p>AgentScope 的 Channel 系统支持多个渠道同时接入：
 * <ul>
 *   <li>{@link ChannelManager} — 管理多个 Channel</li>
 *   <li>{@link Channel} — 渠道接口（ChatUI、Webhook、Slack 等）</li>
 *   <li>{@link ChannelConfig} — 渠道配置（channelId, defaultAgentId, dmScope）</li>
 * </ul>
 *
 * <p>路由规则：消息根据 channelId 路由到对应 Channel，
 * Channel 根据 ChannelConfig 路由到 Agent。
 *
 * <p>本模块演示注册多个 ChatUI 渠道到同一个 Agent。
 */
@Component
public class ChannelsAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;
    private volatile ChannelManager channelManager;

    public ChannelsAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public String chat(String message) {
        return agent().call(
                new io.agentscope.core.message.UserMessage(message),
                io.agentscope.core.agent.RuntimeContext.builder()
                        .sessionId("channels-demo").userId("alice").build())
                .block().getTextContent();
    }

    /** 列出已注册的渠道。 */
    public String listChannels() {
        var ids = channelManager().channelIds();
        return "已注册渠道：" + String.join(", ", ids);
    }

    private ChannelManager channelManager() {
        ChannelManager local = channelManager;
        if (local == null) {
            synchronized (this) {
                local = channelManager;
                if (local == null) {
                    local = new ChannelManager();
                    ChatUiChannel ch1 = ChatUiChannel.create(ChannelConfig.of("chat-1"));
                    ChatUiChannel ch2 = ChatUiChannel.create(ChannelConfig.of("chat-2"));
                    local.register(ch1);
                    local.register(ch2);
                    HarnessGateway gateway = HarnessGateway.create(local);
                    gateway.bindMainAgent(agent());
                    local.initAll(gateway);
                    local.startAll();
                    channelManager = local;
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
                            .name("channels-agent")
                            .sysPrompt("你是一个多渠道助手。")
                            .model(model)
                            .workspace(Paths.get(".agentscope/workspace-channels"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }
}