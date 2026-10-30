package com.yineng.bpe.distributedsolutions.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Outbox 本地消息表实体。
 * <p>业务代码与业务数据在同一个本地事务中写入本表，保证「业务成功则事件必然存在」；
 * 之后由 {@link OutboxRelay} 定时任务捞取并消费，实现最终一致。</p>
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "outbox_messages",
        indexes = @Index(name = "idx_status_next_retry", columnList = "status, next_retry_at"))
public class OutboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 聚合类型，如 ORDER，用于标识事件归属的业务域。
     */
    @Column(name = "aggregate_type", nullable = false, length = 64)
    private String aggregateType;

    /**
     * 聚合业务 ID，如订单号，便于问题追溯。
     */
    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId;

    /**
     * 事件类型，如 ORDER_CREATED。
     */
    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    /**
     * 事件内容（JSON），消费方据此执行业务。
     */
    @Column(name = "payload", length = 4096)
    private String payload;

    /**
     * 消息状态。
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private OutboxStatus status;

    /**
     * 已失败重试次数。
     */
    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    /**
     * 下次允许被捞取的时间（退避机制）。
     */
    @Column(name = "next_retry_at", nullable = false)
    private LocalDateTime nextRetryAt;

    /**
     * 创建时间。
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 消费成功时间。
     */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public static OutboxMessage pending(String aggregateType, String aggregateId,
                                        String eventType, String payload) {
        OutboxMessage message = new OutboxMessage();
        message.aggregateType = aggregateType;
        message.aggregateId = aggregateId;
        message.eventType = eventType;
        message.payload = payload;
        message.status = OutboxStatus.PENDING;
        message.retryCount = 0;
        message.createdAt = LocalDateTime.now();
        message.nextRetryAt = message.createdAt;
        return message;
    }
}
