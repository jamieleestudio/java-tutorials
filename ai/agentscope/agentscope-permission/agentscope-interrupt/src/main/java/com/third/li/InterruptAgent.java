package com.third.li;

import io.agentscope.core.agent.Agent;
import io.agentscope.core.agent.Event;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.agent.StreamOptions;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 实时打断 Agent：演示 {@link Agent#interrupt()} 能力——
 * Agent 运行中可被外部打断，立即中止当前 ReAct 循环。
 *
 * <p>{@link io.agentscope.core.agent.Agent} 接口提供两个打断方法：
 * <ul>
 *   <li>{@code interrupt()} —— 无消息打断</li>
 *   <li>{@code interrupt(Msg)} —— 带用户消息打断（打断后可追加一条消息）</li>
 * </ul>
 *
 * <p>本例用流式调用（{@code stream}）演示：在另一个线程里启动 Agent，
 * 主线程调用 {@code interrupt()} 中止它，观察流提前结束。
 */
@Component
public class InterruptAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-interrupt";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public InterruptAgent(
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

    /**
     * 流式调用并在收到第一个事件后立即 interrupt，
     * 演示运行时打断能力。
     */
    public String chatAndInterrupt(String message) {
        HarnessAgent a = agent();
        AtomicReference<String> firstEvent = new AtomicReference<>("（未产生事件即被打断）");
        reactor.core.publisher.Flux<Event> stream = a.stream(
                java.util.List.of(new UserMessage(message)), StreamOptions.defaults(), runtimeContext());
        try {
            stream.take(1).doOnNext(e -> firstEvent.set(e.getType().name())).blockLast();
        } finally {
            a.interrupt();
        }
        return "首个事件类型：" + firstEvent.get() + "，已发出 interrupt()";
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
                            .name("interrupt-agent")
                            .sysPrompt("你是一个智能助手。运行中可被外部打断。")
                            .model(model)
                            .workspace(Paths.get(WORKSPACE_DIR))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("interrupt-demo").userId("alice").build();
    }
}