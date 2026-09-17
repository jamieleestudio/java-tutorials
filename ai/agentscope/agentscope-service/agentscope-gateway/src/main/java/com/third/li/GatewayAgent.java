package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.gateway.ChannelManager;
import io.agentscope.harness.agent.gateway.GatewayBootstrap;
import io.agentscope.harness.agent.gateway.HarnessGateway;
import io.agentscope.harness.agent.gateway.MsgContext;
import io.agentscope.harness.agent.gateway.channel.chatui.ChatUiChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.List;

/**
 * 网关服务（HarnessGateway + GatewayBootstrap）。
 *
 * <p>AgentScope 的 Gateway 是 Agent 的<b>统一入口</b>：
 * <ul>
 *   <li>管理多个 Channel（ChatUI、Slack、Webhook 等）</li>
 *   <li>路由消息到正确的 Agent</li>
 *   <li>支持 Subagent 暴露和远程调用</li>
 * </ul>
 *
 * <p>{@link GatewayBootstrap} 简化网关创建：
 * <pre>
 * GatewayBootstrap.builder()
 *     .agent(mainAgent)
 *     .build()
 *     .start();
 * </pre>
 *
 * <p>本模块演示 Gateway 的基本使用——通过 gateway.run() 而不是 agent.call()。
 */
@Component
public class GatewayAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;
    private volatile HarnessGateway gateway;

    public GatewayAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    /** 直接调用 Agent（不经过 Gateway）。 */
    public String chat(String message) {
        return agent().call(new UserMessage(message), runtimeContext()).block().getTextContent();
    }

    /** 通过 Gateway 调用（模拟 Channel 入站）。 */
    public String gatewayChat(String message) {
        MsgContext ctx = MsgContext.defaultContext();
        List<Msg> msgs = List.of(new UserMessage(message));
        return gateway().run(ctx, msgs)
                .map(Msg::getTextContent)
                .block();
    }

    private HarnessGateway gateway() {
        HarnessGateway local = gateway;
        if (local == null) {
            synchronized (this) {
                local = gateway;
                if (local == null) {
                    local = HarnessGateway.create(new ChannelManager());
                    local.bindMainAgent(agent());
                    gateway = local;
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
                            .name("gateway-agent")
                            .sysPrompt("你是一个通过网关服务的助手。")
                            .model(model)
                            .workspace(Paths.get(".agentscope/workspace-gateway"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("gateway-demo").userId("alice").build();
    }
}