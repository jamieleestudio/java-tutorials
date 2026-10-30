package com.yineng.bpe.distributedsolutions.outbox;

/**
 * Outbox 消息状态。
 * <ul>
 *     <li>PENDING：待处理，定时任务只捞取到期的（next_retry_at &lt;= now）PENDING 记录</li>
 *     <li>PUBLISHED：已成功消费（本地业务已执行 / 消息已投递）</li>
 *     <li>FAILED：重试超过上限，进入死信状态，需要人工介入</li>
 * </ul>
 */
public enum OutboxStatus {

    PENDING,

    PUBLISHED,

    FAILED
}
