package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.hook.PreCallEvent;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Trigger 模式（反应式触发）。
 *
 * <p>在 Agent 调用前通过 {@link Hook} 检查触发条件，命中则自动改写输入
 * （注入上下文 / 追加系统提示 / 计数告警）。
 *
 * <p>与 Embabel 的 {@code @Action(trigger = X.class)} 对照——Embabel 是"动作触发时机"；
 * AgentScope 用 {@code Hook.onEvent(PreCallEvent)} 在<b>调用入口</b>拦截，
 * 更适合做跨 Agent 的统一触发逻辑（审计、告警、上下文注入）。
 *
 * <p>本模块演示 3 类触发：
 * <ul>
 *   <li>关键词触发：消息含"紧急" → 注入紧急处理上下文</li>
 *   <li>频率触发：调用次数达到阈值 → 追加提醒</li>
 *   <li>审计触发：每次调用记录日志</li>
 * </ul>
 */
@Component
public class TriggerAgent {

    private static final Logger log = LoggerFactory.getLogger(TriggerAgent.class);

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private final AtomicInteger callCount = new AtomicInteger(0);
    private volatile HarnessAgent agent;

    public TriggerAgent(
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

    /** 查看触发统计。 */
    public String stats() {
        return "累计触发调用次数：" + callCount.get() + "（每 3 次触发频率提醒）";
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
                            .name("trigger-agent")
                            .sysPrompt("你是一个客服助手。")
                            .model(model)
                            .hook(new TriggerHook())
                            .workspace(Paths.get(".agentscope/agentscope-trigger"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("trigger").userId("alice").build();
    }

    /**
     * 反应式触发 Hook：在每次 Agent 调用前执行。
     */
    public class TriggerHook implements Hook {

        @Override
        public int priority() {
            return 10;
        }

        @Override
        public <T extends HookEvent> Mono<T> onEvent(T event) {
            if (event instanceof PreCallEvent pre) {
                int count = callCount.incrementAndGet();
                boolean urgent = pre.getInputMessages().stream()
                        .anyMatch(m -> m.getTextContent() != null
                                && m.getTextContent().contains("紧急"));

                if (urgent) {
                    log.warn("[trigger] 检测到【紧急】关键词，注入紧急处理上下文（第 {} 次调用）", count);
                    pre.getInputMessages().add(UserMessage.builder()
                            .content(io.agentscope.core.message.TextBlock.builder()
                                    .text("（系统注入）这是紧急请求，" +
                                            "请优先处理并标注【紧急响应】")
                                    .build())
                            .build());
                }

                if (count % 3 == 0) {
                    log.info("[trigger] 第 {} 次调用，触发频率提醒", count);
                    pre.getInputMessages().add(UserMessage.builder()
                            .content(io.agentscope.core.message.TextBlock.builder()
                                    .text("（系统注入）注意：调用已累计 "
                                            + count + " 次，回复请尽量简洁。")
                                    .build())
                            .build());
                }

                log.info("[trigger] 审计：第 {} 次调用", count);
            }
            return Mono.just(event);
        }
    }
}