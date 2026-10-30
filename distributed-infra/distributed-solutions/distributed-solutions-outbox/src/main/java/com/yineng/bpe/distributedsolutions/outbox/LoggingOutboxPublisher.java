package com.yineng.bpe.distributedsolutions.outbox;

import lombok.extern.slf4j.Slf4j;

/**
 * 默认消费实现：仅打印消息内容，用于演示与验证状态流转。
 * 业务方定义自己的 {@link OutboxPublisher} Bean 后自动替换本实现。
 */
@Slf4j
public class LoggingOutboxPublisher implements OutboxPublisher {

    @Override
    public void publish(OutboxMessage message) {
        log.info("[LoggingOutboxPublisher] 消费事件: type={}, aggregate={}/{}, payload={}",
                message.getEventType(), message.getAggregateType(), message.getAggregateId(), message.getPayload());
    }
}
