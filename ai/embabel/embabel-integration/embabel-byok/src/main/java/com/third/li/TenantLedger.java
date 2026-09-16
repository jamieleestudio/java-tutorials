package com.third.li;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 租户用量台账：累计每个租户的调用次数、token 与成本，并执行**成本上限**。
 *
 * <p>说明：框架自带 {@code Budget}（cost/actions/tokens 三个上限 + 对应早停策略），
 * 但它要通过 {@code ProcessOptions} 传入（Java 侧构造参数较多）。
 * 这里用"进程跑完后读用量"的方式实现应用级治理，效果等价且更易读：
 * {@code AgentProcess.totalUsage()} / {@code totalCost()}。
 */
@Component
public class TenantLedger {

    private final double costLimit;

    private final Map<String, AtomicInteger> calls = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> tokens = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> microCosts = new ConcurrentHashMap<>();

    public TenantLedger(@Value("${demo.tenant-cost-limit}") double costLimit) {
        this.costLimit = costLimit;
    }

    /** 记录一次调用，返回是否**已超预算**（超了就应拒绝后续请求）。 */
    public boolean record(String tenantId, Integer usedTokens, double cost) {
        calls.computeIfAbsent(tenantId, key -> new AtomicInteger()).incrementAndGet();
        tokens.computeIfAbsent(tenantId, key -> new AtomicLong()).addAndGet(usedTokens == null ? 0 : usedTokens);
        microCosts.computeIfAbsent(tenantId, key -> new AtomicLong())
                .addAndGet(Math.round(cost * 1_000_000));
        return costOf(tenantId) > costLimit;
    }

    public boolean overBudget(String tenantId) {
        return costOf(tenantId) > costLimit;
    }

    public Map<String, Object> snapshot(String tenantId) {
        return Map.of(
                "tenantId", tenantId,
                "calls", calls.getOrDefault(tenantId, new AtomicInteger()).get(),
                "tokens", tokens.getOrDefault(tenantId, new AtomicLong()).get(),
                "cost", costOf(tenantId),
                "costLimit", costLimit);
    }

    private double costOf(String tenantId) {
        return microCosts.getOrDefault(tenantId, new AtomicLong()).get() / 1_000_000.0;
    }
}
