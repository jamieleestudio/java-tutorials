package com.yineng.bpe.distributedsolutions.outbox.it;

import com.yineng.bpe.distributedsolutions.outbox.DemoOrder;
import com.yineng.bpe.distributedsolutions.outbox.DemoOrderRepository;
import com.yineng.bpe.distributedsolutions.outbox.OutboxMessage;
import com.yineng.bpe.distributedsolutions.outbox.OutboxMessageRepository;
import com.yineng.bpe.distributedsolutions.outbox.OutboxRelay;
import com.yineng.bpe.distributedsolutions.outbox.OutboxStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Outbox Relay 集成测试：连接真实 MySQL 8，验证 SKIP LOCKED 认领、
 * 状态流转、重试退避与同事务回滚。
 * <p>MySQL 不可达时自动跳过（提示先 {@code docker compose up -d}），
 * 不让基础设施缺失破坏构建。</p>
 */
@SpringBootTest(classes = OutboxTestApplication.class, properties = {
        "outbox.relay.enabled=false",
        "outbox.relay.batch-size=10",
        "outbox.relay.max-retries=3",
        "outbox.relay.initial-backoff=100ms",
        "outbox.relay.backoff-multiplier=2.0"
})
class OutboxRelayTest {

    private static final String JDBC_URL = System.getProperty("mysql.url",
            "jdbc:mysql://localhost:33061/distributed_solutions?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai");
    private static final String JDBC_USER = System.getProperty("mysql.user", "root");
    private static final String JDBC_PASSWORD = System.getProperty("mysql.password", "root");

    @Autowired
    private OutboxMessageRepository outboxRepository;

    @Autowired
    private DemoOrderRepository orderRepository;

    @Autowired
    private OutboxRelay outboxRelay;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private RecordingPublisher publisher;

    @BeforeAll
    static void requireMysql() throws SQLException {
        try (Connection ignored = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
        } catch (SQLException e) {
            assumeTrue(false, "MySQL 不可达，请先在模块目录执行 docker compose up -d : " + e.getMessage());
        }
    }

    @BeforeEach
    void cleanUp() {
        outboxRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        publisher.reset();
    }

    @Test
    void relayOnce_publishes_due_message_and_marks_published() {
        OutboxMessage message = savePendingMessage("ORDER-1");

        int processed = outboxRelay.relayOnce();

        assertThat(processed).isEqualTo(1);
        OutboxMessage reloaded = outboxRepository.findById(message.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
        assertThat(reloaded.getPublishedAt()).isNotNull();
        assertThat(reloaded.getRetryCount()).isZero();
        assertThat(publisher.getInvocations()).isEqualTo(1);
        assertThat(publisher.getProcessedIds()).containsExactly(String.valueOf(message.getId()));
    }

    @Test
    void relayOnce_skips_messages_not_due_yet() {
        OutboxMessage message = OutboxMessage.pending("ORDER", "ORDER-FUTURE-1", "ORDER_CREATED", "{}");
        message.setNextRetryAt(LocalDateTime.now().plusHours(1));
        outboxRepository.saveAndFlush(message);

        int processed = outboxRelay.relayOnce();

        assertThat(processed).isZero();
        OutboxMessage reloaded = outboxRepository.findById(message.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(publisher.getInvocations()).isZero();
    }

    @Test
    void relayOnce_retries_with_backoff_then_marks_failed_after_max_retries() throws InterruptedException {
        OutboxMessage message = savePendingMessage("ORDER-FAIL-1");
        publisher.setFailPolicy(m -> "ORDER-FAIL-1".equals(m.getAggregateId()));

        assertThat(outboxRelay.relayOnce()).isEqualTo(1);
        OutboxMessage afterFirst = outboxRepository.findById(message.getId()).orElseThrow();
        assertThat(afterFirst.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(afterFirst.getRetryCount()).isEqualTo(1);
        assertThat(afterFirst.getNextRetryAt()).isAfter(afterFirst.getCreatedAt());

        assertThat(outboxRelay.relayOnce()).isZero();
        assertThat(publisher.getInvocations()).isEqualTo(1);

        Thread.sleep(150);
        assertThat(outboxRelay.relayOnce()).isEqualTo(1);
        OutboxMessage afterSecond = outboxRepository.findById(message.getId()).orElseThrow();
        assertThat(afterSecond.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(afterSecond.getRetryCount()).isEqualTo(2);

        Thread.sleep(250);
        assertThat(outboxRelay.relayOnce()).isEqualTo(1);
        OutboxMessage afterThird = outboxRepository.findById(message.getId()).orElseThrow();
        assertThat(afterThird.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(afterThird.getRetryCount()).isEqualTo(3);
        assertThat(publisher.getInvocations()).isEqualTo(3);
    }

    @Test
    void concurrent_relays_claim_disjoint_batches_without_duplicates() throws InterruptedException {
        int total = 20;
        List<Long> ids = IntStream.rangeClosed(1, total)
                .mapToObj(i -> savePendingMessage("ORDER-C-" + i).getId())
                .toList();

        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger totalProcessed = new AtomicInteger();
        for (int i = 0; i < 4; i++) {
            executor.submit(() -> {
                start.await();
                totalProcessed.addAndGet(outboxRelay.relayOnce());
                return null;
            });
        }
        start.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(30, TimeUnit.SECONDS)).isTrue();

        assertThat(totalProcessed.get()).isEqualTo(total);
        assertThat(publisher.getInvocations()).isEqualTo(total);
        assertThat(publisher.getProcessedIds()).hasSize(total);
        assertThat(outboxRepository.findAll())
                .allSatisfy(m -> assertThat(m.getStatus()).isEqualTo(OutboxStatus.PUBLISHED));
    }

    @Test
    void rollback_leaves_no_outbox_row_and_no_business_row() {
        long outboxBefore = outboxRepository.count();
        long ordersBefore = orderRepository.count();

        try {
            transactionTemplate.executeWithoutResult(status -> {
                orderRepository.save(new DemoOrder("ORDER-ROLLBACK-1"));
                outboxRepository.save(OutboxMessage.pending("ORDER", "ORDER-ROLLBACK-1", "ORDER_CREATED", "{}"));
                throw new IllegalStateException("模拟业务异常，同事务回滚");
            });
        } catch (IllegalStateException expected) {
        }

        assertThat(outboxRepository.count()).isEqualTo(outboxBefore);
        assertThat(orderRepository.count()).isEqualTo(ordersBefore);
    }

    private OutboxMessage savePendingMessage(String aggregateId) {
        OutboxMessage message = OutboxMessage.pending("ORDER", aggregateId, "ORDER_CREATED",
                "{\"orderNo\":\"" + aggregateId + "\"}");
        return outboxRepository.saveAndFlush(message);
    }
}
