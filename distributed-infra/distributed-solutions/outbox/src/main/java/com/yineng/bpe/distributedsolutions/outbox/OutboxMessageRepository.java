package com.yineng.bpe.distributedsolutions.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outbox 消息数据访问。
 * <p>{@link #claimBatch} 使用 MySQL 8.0+ 的 {@code FOR UPDATE SKIP LOCKED}：
 * 多个 relay 实例并发扫描时，各自锁住不重叠的行，跳过已被其他事务锁定的行，
 * 既不会互相阻塞，也不会重复认领同一条消息。</p>
 */
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {

    /**
     * 认领一批到期（PENDING 且 next_retry_at 已到）的消息。
     * 必须在事务内调用，行锁持续到事务提交。
     */
    @Query(value = """
            SELECT * FROM outbox_messages
            WHERE status = 'PENDING' AND next_retry_at <= :now
            ORDER BY id
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxMessage> claimBatch(@Param("now") LocalDateTime now, @Param("limit") int limit);
}
