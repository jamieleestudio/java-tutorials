package com.yineng.bpe.distributedsolutions.outbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Outbox 定时任务（relay）：轮询认领 -> 逐条消费 -> 状态流转。
 * <p>认领与消费在同一个事务中完成：
 * 认领使用 {@code FOR UPDATE SKIP LOCKED} 保证多实例并发下不重复消费，
 * 行锁持有到事务提交，因此消费动作（本地业务执行）与状态标记是原子的。</p>
 * <p>语义为至少一次（at-least-once）：事务提交前宕机，已消费但未提交的消息仍是
 * PENDING，会被再次捞取，因此 {@link OutboxPublisher} 实现必须幂等。</p>
 */
@Slf4j
public class OutboxRelay {

    private final OutboxMessageRepository repository;

    private final OutboxPublisher publisher;

    private final OutboxRelayProperties properties;

    private final TransactionTemplate transactionTemplate;

    public OutboxRelay(OutboxMessageRepository repository,
                       OutboxPublisher publisher,
                       OutboxRelayProperties properties,
                       TransactionTemplate transactionTemplate) {
        this.repository = repository;
        this.publisher = publisher;
        this.properties = properties;
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * 定时入口，仅当 outbox.relay.enabled=true 时由调度器驱动。
     */
    @Scheduled(fixedDelayString = "${outbox.relay.poll-interval:1s}")
    public void scheduledRelay() {
        relayOnce();
    }

    /**
     * 执行一轮消费：认领一批到期消息并逐条处理。
     *
     * @return 本轮实际处理（含转入 FAILED）的消息条数
     */
    public int relayOnce() {
        Integer processed = transactionTemplate.execute(status -> {
            LocalDateTime now = LocalDateTime.now();
            List<OutboxMessage> batch = repository.claimBatch(now, properties.getBatchSize());
            if (batch.isEmpty()) {
                return 0;
            }
            log.info("Outbox relay 认领 {} 条消息", batch.size());
            batch.forEach(this::handle);
            return batch.size();
        });
        return processed == null ? 0 : processed;
    }

    private void handle(OutboxMessage message) {
        try {
            publisher.publish(message);
            message.setStatus(OutboxStatus.PUBLISHED);
            message.setPublishedAt(LocalDateTime.now());
            log.info("Outbox relay 消费成功: id={}, type={}, aggregate={}/{}, retryCount={}",
                    message.getId(), message.getEventType(), message.getAggregateType(),
                    message.getAggregateId(), message.getRetryCount());
        } catch (Exception e) {
            int retryCount = message.getRetryCount() + 1;
            message.setRetryCount(retryCount);
            if (retryCount >= properties.getMaxRetries()) {
                message.setStatus(OutboxStatus.FAILED);
                log.error("Outbox relay 重试超限，转 FAILED 死信: id={}, type={}, aggregate={}/{}",
                        message.getId(), message.getEventType(),
                        message.getAggregateType(), message.getAggregateId(), e);
            } else {
                Duration backoff = nextBackoff(retryCount);
                message.setNextRetryAt(LocalDateTime.now().plus(backoff));
                log.warn("Outbox relay 消费失败，第 {} 次重试将于 {} 后进行: id={}, 原因: {}",
                        retryCount, backoff, message.getId(), e.getMessage());
            }
        }
    }

    /**
     * 指数退避：initialBackoff * multiplier^(retryCount-1)。
     */
    private Duration nextBackoff(int retryCount) {
        double factor = Math.pow(properties.getBackoffMultiplier(), retryCount - 1);
        return Duration.ofMillis((long) (properties.getInitialBackoff().toMillis() * factor));
    }
}
