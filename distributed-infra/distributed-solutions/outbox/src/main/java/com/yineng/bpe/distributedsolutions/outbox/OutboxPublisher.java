package com.yineng.bpe.distributedsolutions.outbox;

/**
 * Outbox 消息消费 SPI。
 * <p>捞取到事件后的处理动作由实现方决定：</p>
 * <ul>
 *     <li>直接执行本地业务（如更新订单状态、发通知）—— 定时任务消费事件业务</li>
 *     <li>投递到 MQ 交给下游服务 —— 经典本地消息表用法（可后续扩展 KafkaOutboxPublisher）</li>
 * </ul>
 * <p>注意：本模式是至少一次投递（at-least-once），实现方必须保证幂等。</p>
 */
public interface OutboxPublisher {

    /**
     * 消费一条 outbox 消息。
     *
     * @throws Exception 消费失败，由 relay 记录重试或转入 FAILED
     */
    void publish(OutboxMessage message) throws Exception;
}
