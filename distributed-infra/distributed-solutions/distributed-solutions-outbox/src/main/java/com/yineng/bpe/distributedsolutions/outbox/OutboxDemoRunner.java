package com.yineng.bpe.distributedsolutions.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Outbox 演示流程：
 * <ol>
 *     <li>场景一：订单与事件同事务提交，定时任务捞取后由本地处理器执行业务，状态 PENDING -&gt; PUBLISHED</li>
 *     <li>场景二：业务中途抛异常，同事务回滚，事件不会泄漏到表中（不多）</li>
 *     <li>场景三：处理器前两次失败，观察重试与指数退避，最终成功（不丢、最终一致）</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxDemoRunner implements ApplicationRunner {

    private final OutboxMessageRepository outboxRepository;

    private final DemoOrderRepository orderRepository;

    private final TransactionTemplate transactionTemplate;

    private final ApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) {
        outboxRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        log.info("========== Outbox Demo 开始 ==========");

        scenario1_sameTransactionPublish();
        scenario2_rollbackLeavesNothing();
        scenario3_retryWithBackoff();

        printSummary();
        System.exit(SpringApplication.exit(applicationContext));
    }

    private void scenario1_sameTransactionPublish() {
        log.info("---- 场景一：业务与事件同事务落库，定时任务捞取执行 ----");
        transactionTemplate.executeWithoutResult(status -> {
            orderRepository.save(new DemoOrder("ORDER-OK-1"));
            outboxRepository.save(OutboxMessage.pending("ORDER", "ORDER-OK-1", "ORDER_CREATED",
                    "{\"orderNo\":\"ORDER-OK-1\",\"amount\":99.9}"));
        });
        log.info("订单 + outbox 事件已在同一事务提交，等待定时任务捞取...");
        awaitStatus("ORDER-OK-1", OutboxStatus.PUBLISHED, Duration.ofSeconds(10));
    }

    private void scenario2_rollbackLeavesNothing() {
        log.info("---- 场景二：业务异常导致同事务回滚，事件不多不少 ----");
        long before = outboxRepository.count();
        try {
            transactionTemplate.executeWithoutResult(status -> {
                orderRepository.save(new DemoOrder("ORDER-FAIL-1"));
                outboxRepository.save(OutboxMessage.pending("ORDER", "ORDER-FAIL-1", "ORDER_CREATED",
                        "{\"orderNo\":\"ORDER-FAIL-1\",\"amount\":1.0}"));
                throw new IllegalStateException("模拟业务异常，整个事务回滚");
            });
        } catch (IllegalStateException e) {
            log.info("捕获业务异常: {}", e.getMessage());
        }
        long after = outboxRepository.count();
        log.info("回滚前 outbox 总行数 = {}，回滚后 = {}，{}",
                before, after, before == after ? "无事件泄漏 ✔" : "异常！事件泄漏了");
    }

    private void scenario3_retryWithBackoff() {
        log.info("---- 场景三：消费失败重试（指数退避），最终成功 ----");
        transactionTemplate.executeWithoutResult(status -> {
            orderRepository.save(new DemoOrder("ORDER-RETRY-DEMO"));
            outboxRepository.save(OutboxMessage.pending("ORDER", "ORDER-RETRY-DEMO", "ORDER_CREATED",
                    "{\"orderNo\":\"ORDER-RETRY-DEMO\",\"amount\":66.6}"));
        });
        log.info("事件已提交，处理器前两次会失败，观察重试日志...");
        awaitStatus("ORDER-RETRY-DEMO", OutboxStatus.PUBLISHED, Duration.ofSeconds(30));
    }

    private void awaitStatus(String aggregateId, OutboxStatus expected, Duration timeout) {
        LocalDateTime deadline = LocalDateTime.now().plus(timeout);
        while (LocalDateTime.now().isBefore(deadline)) {
            Optional<OutboxMessage> found = outboxRepository.findAll().stream()
                    .filter(m -> aggregateId.equals(m.getAggregateId()))
                    .findFirst();
            if (found.isPresent() && found.get().getStatus() == expected) {
                OutboxMessage message = found.get();
                log.info("{} 已到达状态 {}（retryCount={}）",
                        aggregateId, expected, message.getRetryCount());
                return;
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        log.warn("等待 {} 超时，未在 {} 内到达状态 {}", aggregateId, timeout, expected);
    }

    private void printSummary() {
        log.info("========== Demo 结果汇总 ==========");
        List<OutboxMessage> all = outboxRepository.findAll();
        all.forEach(message -> log.info("outbox: id={}, aggregate={}, type={}, status={}, retryCount={}, nextRetryAt={}",
                message.getId(), message.getAggregateId(), message.getEventType(),
                message.getStatus(), message.getRetryCount(), message.getNextRetryAt()));
        orderRepository.findAll().forEach(order -> log.info("order: id={}, orderNo={}, status={}",
                order.getId(), order.getOrderNo(), order.getStatus()));
    }
}
