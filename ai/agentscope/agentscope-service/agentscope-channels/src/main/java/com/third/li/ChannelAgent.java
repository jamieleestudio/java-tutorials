package com.third.li;

import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.channel.dingtalk.DingTalkChannel;
import io.agentscope.extensions.channel.feishu.FeishuChannel;
import io.agentscope.extensions.channel.wecom.WeComChannel;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.gateway.HarnessGateway;
import io.agentscope.harness.agent.gateway.MsgContext;
import io.agentscope.harness.agent.gateway.channel.Channel;
import io.agentscope.harness.agent.gateway.channel.ChannelConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * IM 渠道 Agent：把 Agent 接入钉钉 / 飞书 / 企业微信 三种 IM 渠道。
 *
 * <p>每个 IM 渠道都是 {@link Channel} 实现，通过 {@code fromProperties} 工厂创建：
 * <ul>
 *   <li>{@link DingTalkChannel#fromProperties} —— 钉钉（需 appKey/appSecret/robotCode）</li>
 *   <li>{@link FeishuChannel#fromProperties} —— 飞书（需 appId/appSecret）</li>
 *   <li>{@link WeComChannel#fromProperties} —— 企业微信（需 token/aesKey）</li>
 * </ul>
 *
 * <p>创建后注册到 {@link HarnessGateway}，IM 消息进来后路由给主 Agent。
 * 凭据从配置/环境变量读取，未配置时渠道不会真正连接 IM 平台。
 */
@Component
public class ChannelAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-channels";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private final String dingtalkAppKey;
    private final String dingtalkAppSecret;
    private final String feishuAppId;
    private final String feishuAppSecret;
    private final String wecomToken;
    private final String wecomAesKey;
    private volatile HarnessGateway gateway;
    private volatile HarnessAgent mainAgent;

    public ChannelAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl,
            @Value("${agentscope.dingtalk.app-key:}") String dingtalkAppKey,
            @Value("${agentscope.dingtalk.app-secret:}") String dingtalkAppSecret,
            @Value("${agentscope.feishu.app-id:}") String feishuAppId,
            @Value("${agentscope.feishu.app-secret:}") String feishuAppSecret,
            @Value("${agentscope.wecom.token:}") String wecomToken,
            @Value("${agentscope.wecom.aes-key:}") String wecomAesKey) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.dingtalkAppKey = dingtalkAppKey;
        this.dingtalkAppSecret = dingtalkAppSecret;
        this.feishuAppId = feishuAppId;
        this.feishuAppSecret = feishuAppSecret;
        this.wecomToken = wecomToken;
        this.wecomAesKey = wecomAesKey;
    }

    public String chat(String message) {
        HarnessGateway gw = gateway();
        MsgContext ctx = MsgContext.defaultContext();
        Msg reply = gw.run(ctx, List.of(new UserMessage(message))).block();
        return reply != null ? reply.getTextContent() : "（无回复）";
    }

    public List<String> registeredChannels() {
        return gateway().channelManager().channelIds();
    }

    private HarnessGateway gateway() {
        HarnessGateway local = gateway;
        if (local == null) {
            synchronized (this) {
                local = gateway;
                if (local == null) {
                    local = HarnessGateway.create();
                    local.bindMainAgent(mainAgent());
                    registerDingTalk(local);
                    registerFeishu(local);
                    registerWeCom(local);
                    local.channelManager().initAll(local);
                    gateway = local;
                }
            }
        }
        return local;
    }

    private void registerDingTalk(HarnessGateway gw) {
        if (dingtalkAppKey.isBlank() || dingtalkAppSecret.isBlank()) return;
        Channel ch = DingTalkChannel.fromProperties("dingtalk",
                ChannelConfig.of("dingtalk", "im-agent"),
                Map.of("appKey", dingtalkAppKey, "appSecret", dingtalkAppSecret));
        gw.channelManager().register(ch);
    }

    private void registerFeishu(HarnessGateway gw) {
        if (feishuAppId.isBlank() || feishuAppSecret.isBlank()) return;
        Channel ch = FeishuChannel.fromProperties("feishu",
                ChannelConfig.of("feishu", "im-agent"),
                Map.of("appId", feishuAppId, "appSecret", feishuAppSecret));
        gw.channelManager().register(ch);
    }

    private void registerWeCom(HarnessGateway gw) {
        if (wecomToken.isBlank() || wecomAesKey.isBlank()) return;
        Channel ch = WeComChannel.fromProperties("wecom",
                ChannelConfig.of("wecom", "im-agent"),
                Map.of("token", wecomToken, "aesKey", wecomAesKey));
        gw.channelManager().register(ch);
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
                            .name("channels-agent")
                            .sysPrompt("你是一个 IM 助手，消息从钉钉/飞书/企业微信渠道汇聚。")
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