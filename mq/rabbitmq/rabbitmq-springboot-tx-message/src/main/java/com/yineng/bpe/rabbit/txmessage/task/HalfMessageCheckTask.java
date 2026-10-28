package com.yineng.bpe.rabbit.txmessage.task;

import com.yineng.bpe.rabbit.txmessage.message.PointsMessage;
import com.yineng.bpe.rabbit.txmessage.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "tx-message.check.enabled", havingValue = "true", matchIfMissing = true)
public class HalfMessageCheckTask {

    private static final Logger log = LoggerFactory.getLogger(HalfMessageCheckTask.class);

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;

    public HalfMessageCheckTask(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper,
                                PaymentService paymentService) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.paymentService = paymentService;
    }

    @Scheduled(fixedDelayString = "${tx-message.check.interval-ms:5000}")
    public void check() {
        checkHalfMessages();
        resendStuckCommittedMessages();
    }

    private void checkHalfMessages() {
        List<Map<String, Object>> halfMessages = jdbcTemplate.queryForList(
                "SELECT message_id, biz_key, payload FROM t_tx_message "
                        + "WHERE status = ? AND created_at <= ? ORDER BY id LIMIT 100",
                PaymentService.STATUS_HALF, LocalDateTime.now().minusSeconds(30));

        for (Map<String, Object> msg : halfMessages) {
            String messageId = (String) msg.get("message_id");
            String bizKey = (String) msg.get("biz_key");
            String payload = (String) msg.get("payload");

            log.warn("[Check] 回查半消息（等价于 RocketMQ 消息回查），messageId={}, bizKey={}", messageId, bizKey);
            if (paymentService.orderExists(bizKey)) {
                log.warn("[Check] 回查结果：本地事务已提交，补偿投递消息，messageId={}", messageId);
                paymentService.markCommittedAndSend(messageId, payload);
            } else {
                log.warn("[Check] 回查结果：本地事务未提交/已回滚，半消息回滚，messageId={}", messageId);
                paymentService.markRolledBack(messageId);
            }
        }
    }

    private void resendStuckCommittedMessages() {
        List<Map<String, Object>> stuckMessages = jdbcTemplate.queryForList(
                "SELECT message_id, payload FROM t_tx_message "
                        + "WHERE status = ? AND updated_at <= ? ORDER BY id LIMIT 100",
                PaymentService.STATUS_COMMITTED, LocalDateTime.now().minusSeconds(60));

        for (Map<String, Object> msg : stuckMessages) {
            String messageId = (String) msg.get("message_id");
            String payload = (String) msg.get("payload");
            log.warn("[Check] 已提交消息长时间未收到 Confirm，兜底重发，messageId={}", messageId);
            try {
                PointsMessage message = objectMapper.readValue(payload, PointsMessage.class);
                paymentService.send(messageId, message);
                jdbcTemplate.update("UPDATE t_tx_message SET updated_at = NOW() WHERE message_id = ?", messageId);
            } catch (Exception e) {
                log.error("[Check] 兜底重发失败，messageId={}", messageId, e);
            }
        }
    }
}
