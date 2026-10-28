package com.yineng.bpe.rabbit.reliable.task;

import com.yineng.bpe.rabbit.reliable.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "reliable.compensate.enabled", havingValue = "true", matchIfMissing = true)
public class MessageCompensationTask {

    private static final Logger log = LoggerFactory.getLogger(MessageCompensationTask.class);

    private final JdbcTemplate jdbcTemplate;
    private final PaymentService paymentService;

    public MessageCompensationTask(JdbcTemplate jdbcTemplate, PaymentService paymentService) {
        this.jdbcTemplate = jdbcTemplate;
        this.paymentService = paymentService;
    }

    @Scheduled(fixedDelayString = "${reliable.compensate.interval-ms:5000}")
    public void compensate() {
        int batchSize = 100;
        int maxRetry = 5;
        int backoffSeconds = 30;

        List<Map<String, Object>> dueMessages = jdbcTemplate.queryForList(
                "SELECT message_id, payload, retry_count FROM t_local_message "
                        + "WHERE status = 0 AND next_retry_time <= ? AND retry_count < ? "
                        + "ORDER BY id LIMIT ?",
                LocalDateTime.now(), maxRetry, batchSize);

        for (Map<String, Object> msg : dueMessages) {
            String messageId = (String) msg.get("message_id");
            String payload = (String) msg.get("payload");
            int retryCount = ((Number) msg.get("retry_count")).intValue();

            log.warn("[Compensate] 扫描到待补偿消息，重发，messageId={}, retryCount={}", messageId, retryCount);
            paymentService.resend(messageId, payload);

            int newRetryCount = retryCount + 1;
            if (newRetryCount >= maxRetry) {
                log.error("[Compensate] 消息重试次数已达上限({})，停止补偿，messageId={}，需人工介入", maxRetry, messageId);
            }
            jdbcTemplate.update(
                    "UPDATE t_local_message SET retry_count = ?, next_retry_time = ? WHERE message_id = ?",
                    newRetryCount,
                    LocalDateTime.now().plusSeconds((long) backoffSeconds * newRetryCount),
                    messageId);
        }
    }
}
