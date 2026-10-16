package com.yineng.bpe.distributedsolutions.outbox;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 演示用本地业务处理器：捞取到 ORDER_CREATED 事件后直接执行业务（打印）。
 * <p>对聚合 ID 为 ORDER-RETRY-DEMO 的事件，前两次处理抛出异常，
 * 用于演示失败重试与指数退避。</p>
 */
@Slf4j
public class OrderEventHandler implements OutboxPublisher {

    private final AtomicInteger retryDemoFailures = new AtomicInteger();

    @Override
    public void publish(OutboxMessage message) {
        if ("ORDER-RETRY-DEMO".equals(message.getAggregateId())
                && retryDemoFailures.incrementAndGet() <= 2) {
            throw new IllegalStateException("模拟下游处理故障，第 " + retryDemoFailures.get() + " 次失败");
        }
        log.info("[OrderEventHandler] 执行事件业务: type={}, aggregate={}/{}, payload={}",
                message.getEventType(), message.getAggregateType(), message.getAggregateId(), message.getPayload());
    }
}
