package com.third.li;

import io.agentscope.core.agent.Agent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.AgentEventType;
import io.agentscope.core.middleware.MiddlewareBase;
import io.agentscope.core.middleware.ModelCallInput;
import io.agentscope.core.middleware.ReasoningInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * 自定义预算熔断中间件 —— 填补 AgentScope Java 的空白。
 *
 * <p>AgentScope <b>Python</b> 版内置 {@code BudgetControlMiddleware}，
 * 但 <b>Java</b> 版（2.0.x）没有等价物。本类通过 {@link MiddlewareBase}
 * 的 {@code onReasoning} 钩子自行实现：
 * <ul>
 *   <li>每推理（onReasoning）累加 token 估算</li>
 *   <li>超过 maxTokens 时<strong>短路</strong>——不调 {@code next}，直接返回终止事件</li>
 *   <li>超过 maxIters 时同样短路</li>
 * </ul>
 *
 * <p>关键设计：预算检查发生在 <b>推理之前</b>（而非之后），
 * 这样可以在"下一轮"开始前就阻止，避免不必要的 LLM 调用。
 * 这比 Embabel 的"action 之间检查"更精确——后者会超调一轮。
 *
 * <p><b>洋葱模型短路</b>：不调 {@code next.apply(input)} 时，
 * 后续中间件和核心推理逻辑都不会执行，Flux 直接 complete。
 */
public class BudgetControlMiddleware implements MiddlewareBase {

    private static final Logger log = LoggerFactory.getLogger(BudgetControlMiddleware.class);

    private final int maxTokens;
    private final int maxIters;
    private final AtomicInteger totalTokens = new AtomicInteger(0);
    private final AtomicInteger iterCount = new AtomicInteger(0);

    public BudgetControlMiddleware(int maxTokens, int maxIters) {
        this.maxTokens = maxTokens;
        this.maxIters = maxIters;
    }

    @Override
    public int order() {
        return 5;
    }

    /**
     * 推理阶段：在调模型前检查预算，超限则短路。
     *
     * <p>这是预算控制的<b>核心钩子</b>——每轮 ReAct 循环的推理步骤。
     * 如果不调 {@code next}，模型不会被调用，循环终止。
     */
    @Override
    public Flux<AgentEvent> onReasoning(Agent agent, RuntimeContext ctx, ReasoningInput input,
            Function<ReasoningInput, Flux<AgentEvent>> next) {
        int iter = iterCount.incrementAndGet();
        int estimatedTokens = estimateTokens(input.messages().size(), input.tools().size());
        int cumulative = totalTokens.addAndGet(estimatedTokens);

        log.info("[budget] 第 {} 轮推理，本轮估算 {} tokens，累计 {} / {} tokens",
                iter, estimatedTokens, cumulative, maxTokens);

        if (cumulative >= maxTokens) {
            log.warn("[budget] Token 预算耗尽（{}/{}），短路终止", cumulative, maxTokens);
            return Flux.empty();
        }
        if (iter >= maxIters) {
            log.warn("[budget] 迭代次数达上限（{}/{}），短路终止", iter, maxIters);
            return Flux.empty();
        }
        return next.apply(input);
    }

    /**
     * 模型调用阶段：记录实际 token 用量（如果能拿到的话）。
     *
     * <p>注意：onModelCall 是<b>最内层</b>钩子，在 LLM API 调用前后都会执行。
     * 这里用于精确统计，但预算判断在 onReasoning 中已做。
     */
    @Override
    public Flux<AgentEvent> onModelCall(Agent agent, RuntimeContext ctx, ModelCallInput input,
            Function<ModelCallInput, Flux<AgentEvent>> next) {
        return next.apply(input)
                .doOnNext(event -> {
                    if (event.getType() == AgentEventType.MODEL_CALL_END) {
                        log.debug("[budget] 模型调用完成");
                    }
                });
    }

    /** 粗略估算：每条消息约 100 tokens + 每个工具约 50 tokens。 */
    private int estimateTokens(int msgCount, int toolCount) {
        return msgCount * 100 + toolCount * 50;
    }

    public int getTotalTokens() { return totalTokens.get(); }
    public int getIterCount() { return iterCount.get(); }

    public Mono<String> getReport() {
        return Mono.just(String.format(
                "预算报告：已用 %d/%d tokens，已执行 %d/%d 迭代",
                totalTokens.get(), maxTokens, iterCount.get(), maxIters));
    }
}