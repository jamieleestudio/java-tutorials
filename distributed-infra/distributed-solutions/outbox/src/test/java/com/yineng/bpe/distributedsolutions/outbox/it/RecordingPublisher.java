package com.yineng.bpe.distributedsolutions.outbox.it;

import com.yineng.bpe.distributedsolutions.outbox.OutboxMessage;
import com.yineng.bpe.distributedsolutions.outbox.OutboxPublisher;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * 测试用记录型消费实现：统计调用次数与处理过的消息 ID，可按策略模拟失败。
 * 通过组件扫描注册（OutboxTestApplication 同包），自动替换默认的日志实现。
 */
@Component
public class RecordingPublisher implements OutboxPublisher {

    private final AtomicInteger invocations = new AtomicInteger();

    private final Set<String> processedIds = ConcurrentHashMap.newKeySet();

    private volatile Predicate<OutboxMessage> failPolicy = message -> false;

    @Override
    public void publish(OutboxMessage message) {
        invocations.incrementAndGet();
        if (failPolicy.test(message)) {
            throw new IllegalStateException("模拟下游处理失败: " + message.getAggregateId());
        }
        processedIds.add(String.valueOf(message.getId()));
    }

    public void setFailPolicy(Predicate<OutboxMessage> failPolicy) {
        this.failPolicy = failPolicy;
    }

    public int getInvocations() {
        return invocations.get();
    }

    public Set<String> getProcessedIds() {
        return new HashSet<>(processedIds);
    }

    public void reset() {
        invocations.set(0);
        processedIds.clear();
        failPolicy = message -> false;
    }
}
