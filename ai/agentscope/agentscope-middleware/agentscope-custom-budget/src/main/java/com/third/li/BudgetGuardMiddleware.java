package com.third.li;

import io.agentscope.core.agent.Agent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.AgentResultEvent;
import io.agentscope.core.event.ModelCallEndEvent;
import io.agentscope.core.message.AssistantMessage;
import io.agentscope.core.middleware.AgentInput;
import io.agentscope.core.middleware.MiddlewareBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * 自实现预算熔断中间件：累计 token 用量，超过预算时短路返回。
 *
 * <p>AgentScope Java 版没有内置的"预算"中间件（Python 版有），
 * 这正好用来演示 {@link MiddlewareBase} 洋葱模型的**短路能力**——
 * 在 {@code onAgent} 钩子里检查预算，超限时不调用 {@code next}，
 * 直接返回一个"预算已耗尽"的结果事件。
 *
 * <p>token 用量来自 {@link ModelCallEndEvent#getUsage()}，
 * 通过 {@code Flux.doOnNext} 在事件流中累计。
 */
public class BudgetGuardMiddleware implements MiddlewareBase {

    private static final Logger log = LoggerFactory.getLogger(BudgetGuardMiddleware.class);

    private final int tokenBudget;
    private final AtomicInteger used = new AtomicInteger(0);

    public BudgetGuardMiddleware(int tokenBudget) {
        this.tokenBudget = tokenBudget;
    }

    public int usedTokens() {
        return used.get();
    }

    public int budget() {
        return tokenBudget;
    }

    @Override
    public int order() {
        return 5;
    }

    @Override
    public Flux<AgentEvent> onAgent(Agent agent, RuntimeContext ctx, AgentInput input,
            Function<AgentInput, Flux<AgentEvent>> next) {
        if (used.get() >= tokenBudget) {
            log.warn("[budget] 预算已耗尽：{}/{} tokens，请求被短路", used.get(), tokenBudget);
            AgentEvent denied = new AgentResultEvent(new AssistantMessage(
                    "预算已耗尽（已用 " + used.get() + " / " + tokenBudget + " tokens），本次请求被熔断。"));
            return Flux.just(denied);
        }
        return next.apply(input)
                .doOnNext(e -> {
                    if (e instanceof ModelCallEndEvent mce && mce.getUsage() != null) {
                        int add = mce.getUsage().getTotalTokens();
                        int now = used.addAndGet(add);
                        log.info("[budget] 本次 +{} tokens，累计 {}/{}", add, now, tokenBudget);
                    }
                });
    }
}