package com.third.li;

import com.embabel.agent.api.event.AbstractAgentProcessEvent;
import com.embabel.agent.api.event.AgentProcessEvent;
import com.embabel.agent.api.event.AgenticEventListener;
import com.embabel.agent.core.EarlyTermination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 可观测性（阶段 6）：把每次运行的成本/token/动作数累计起来。
 *
 * <p>这就是 {@code embabel-observability} + {@code embabel-budget} 的组合用法：
 * 用 {@code AgenticEventListener} 采集，用 {@code EarlyTermination} 事件感知熔断。
 */
@Component
public class TicketMetrics implements AgenticEventListener {

    private static final Logger log = LoggerFactory.getLogger(TicketMetrics.class);

    private final AtomicInteger events = new AtomicInteger();
    private final AtomicInteger terminations = new AtomicInteger();
    private final AtomicLong tokens = new AtomicLong();
    private final AtomicLong microCost = new AtomicLong();
    private final AtomicReference<String> lastSummary = new AtomicReference<>("-");

    @Override
    public void onProcessEvent(AgentProcessEvent event) {
        if (event instanceof EarlyTermination termination) {
            terminations.incrementAndGet();
            log.warn("预算熔断：{}", termination.getReason());
            return;
        }
        if (!(event instanceof AbstractAgentProcessEvent processEvent)) {
            return;
        }
        var process = processEvent.getAgentProcess();
        events.incrementAndGet();
        var usage = process.totalUsage();
        if (usage != null && usage.getTotalTokens() != null) {
            tokens.addAndGet(usage.getTotalTokens());
        }
        microCost.addAndGet(Math.round(process.totalCost() * 1_000_000));
        lastSummary.set("process=%s status=%s actions=%d".formatted(
                process.getId(), process.getStatus(), process.getHistory().size()));
    }

    /** 收到的进程事件数（不是"运行次数"——一次运行会产生多个事件）。 */
    public int events() {
        return events.get();
    }

    public int terminations() {
        return terminations.get();
    }

    public long tokens() {
        return tokens.get();
    }

    public double cost() {
        return microCost.get() / 1_000_000.0;
    }

    public String lastSummary() {
        return lastSummary.get();
    }
}
