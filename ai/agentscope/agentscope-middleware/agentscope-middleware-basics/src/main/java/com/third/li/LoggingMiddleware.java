package com.third.li;

import io.agentscope.core.agent.Agent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.middleware.ActingInput;
import io.agentscope.core.middleware.AgentInput;
import io.agentscope.core.middleware.MiddlewareBase;
import io.agentscope.core.middleware.ModelCallInput;
import io.agentscope.core.middleware.ReasoningInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * 自定义中间件：在 ReAct 循环的每个阶段打印日志 + 改写系统提示。
 *
 * <p>{@link MiddlewareBase} 有 5 个 default 方法，覆盖整个循环：
 * <ol>
 *   <li>{@code onAgent} — Agent 调用入口（最外层）</li>
 *   <li>{@code onReasoning} — 每一轮推理（调模型）</li>
 *   <li>{@code onActing} — 每一轮行（调工具）</li>
 *   <li>{@code onModelCall} — 每次 LLM 调用（最内层）</li>
 *   <li>{@code onSystemPrompt} — 系统提示词生成（可改写）</li>
 * </ol>
 *
 * <p>每个方法的最后一个参数是 {@code next}（下一个中间件或核心逻辑）——
 * 这是经典的**洋葱模型**：你可以在调 {@code next} 之前/之后做事情，
 * 也可以不调 {@code next} 来**短路**（比如预算耗尽时直接返回）。
 *
 * <p><b>这是 AgentScope 独有的设计</b>——Embabel 没有"循环钩子"，
 * 它的扩展点是 {@code @Action}（声明式动作），不是"在循环中间插入逻辑"。
 */
public class LoggingMiddleware implements MiddlewareBase {

    private static final Logger log = LoggerFactory.getLogger(LoggingMiddleware.class);

    @Override
    public int order() {
        return 10; // 数值小的先执行
    }

    /** Agent 入口：记录调用开始/结束。 */
    @Override
    public Flux<AgentEvent> onAgent(Agent agent, RuntimeContext ctx, AgentInput input,
            Function<AgentInput, Flux<AgentEvent>> next) {
        log.info("[middleware] Agent '{}' 开始，消息数={}", agent.getName(), input.msgs().size());
        return next.apply(input)
                .doOnComplete(() -> log.info("[middleware] Agent '{}' 完成", agent.getName()));
    }

    /** 推理阶段：记录每轮推理。 */
    @Override
    public Flux<AgentEvent> onReasoning(Agent agent, RuntimeContext ctx, ReasoningInput input,
            Function<ReasoningInput, Flux<AgentEvent>> next) {
        log.info("[middleware] 推理：消息数={}，工具数={}", input.messages().size(), input.tools().size());
        return next.apply(input);
    }

    /** 行动阶段：记录工具调用。 */
    @Override
    public Flux<AgentEvent> onActing(Agent agent, RuntimeContext ctx, ActingInput input,
            Function<ActingInput, Flux<AgentEvent>> next) {
        log.info("[middleware] 行动：{} 个工具调用", input.toolCalls().size());
        return next.apply(input);
    }

    /** 系统提示改写：在原始提示后追加一句。 */
    @Override
    public Mono<String> onSystemPrompt(Agent agent, RuntimeContext ctx, String prompt) {
        String enhanced = prompt + "\n\n（额外要求：回答末尾加一句「——由中间件增强」）";
        log.info("[middleware] 系统提示已改写（原长={}，新长={}）", prompt.length(), enhanced.length());
        return Mono.just(enhanced);
    }
}