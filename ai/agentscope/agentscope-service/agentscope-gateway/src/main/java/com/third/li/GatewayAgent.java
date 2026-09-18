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
 * 网关 Agent：用 {@link HarnessGateway} 把多个渠道（Channel）绑定到 Agent，
 * 统一路由消息。
 *
 * <p>{@link HarnessGateway} 是 AgentScope 的消息网关：
 * <ul>
 *   <li>{@code HarnessGateway.create()} 创建网关</li>
 *   <li>{@code bindMainAgent(agent)} 绑定主 Agent</li>
 *   <li>{@code channelManager.register(channel)} 注渠道</li>
 *   <li>{@code run(msgContext, msgs)} 运行一轮对话</li>
 * </ul>
 *
 * <p>本例注册一个 {@link ChatUiChannel}（内置 Web UI 渠道），
 * 消息从渠道进来后由网关路由给主 Agent 处理。
 */
@Component
public class GatewayAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-gateway";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessGateway gateway;
    private volatile HarnessAgent mainAgent;

    public GatewayAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public String chat(String message) {
        HarnessGateway gw = gateway();
        io.agentscope.harness.agent.gateway.MsgContext ctx =
                io.agentscope.harness.agent.gateway.MsgContext.defaultContext();
        Msg reply = gw.run(ctx, List.of(new UserMessage(message))).block();
        return reply != null ? reply.getTextContent() : "（无回复）";
    }

    private HarnessGateway gateway() {
        HarnessGateway local = gateway;
        if (local == null) {
            synchronized (this) {
                local = gateway;
                if (local == null) {
                    local = HarnessGateway.create();
                    local.bindMainAgent(mainAgent());
                    ChatUiChannel chatUi = ChatUiChannel.create(
                            ChannelConfig.of(ChatUiChannel.CHANNEL_ID, "gateway-agent"));
                    local.channelManager().register(chatUi);
                    local.channelManager().initAll(local);
                    gateway = local;
                }
            }
        }
        return local;
    }

    private HarnessAgent mainAgent() {
        HarnessAgent local = mainAgent;
        if (local == null) {
            synchronized (this) {
                local = mainAgent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("gateway-agent")
                            .sysPrompt("你是一个网关助手，消息从多渠道汇聚后由你统一处理。")
                            .model(model)
                            .workspace(Paths.get(WORKSPACE_DIR))
                            .build();
                    mainAgent = local;
                }
            }
        }
        return local;
    }
}