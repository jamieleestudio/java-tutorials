package com.yineng.bpe.rabbit.txmessage.service;

import com.yineng.bpe.rabbit.txmessage.config.RabbitTxMessageConfig;
import com.yineng.bpe.rabbit.txmessage.message.PointsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    public static final int STATUS_HALF = 0;
    public static final int STATUS_COMMITTED = 1;
    public static final int STATUS_ROLLBACK = 2;
    public static final int STATUS_DELIVERED = 3;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;
    private final TransactionTemplate transactionTemplate;

    public PaymentService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper,
                          RabbitTemplate rabbitTemplate, org.springframework.transaction.PlatformTransactionManager txManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.transactionTemplate = new TransactionTemplate(txManager);
    }

    public void pay(String orderNo, Long userId, BigDecimal amount, int points, boolean fail) {
        String messageId = UUID.randomUUID().toString();
        PointsMessage message = new PointsMessage(orderNo, userId, points);
        String payload = objectMapper.writeValueAsString(message);

        jdbcTemplate.update(
                "INSERT INTO t_tx_message (message_id, biz_key, exchange, routing_key, payload, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                messageId, orderNo, RabbitTxMessageConfig.POINTS_EXCHANGE,
                RabbitTxMessageConfig.POINTS_ROUTING_KEY, payload, STATUS_HALF);
        log.info("[TxMsg] Phase 1：半消息落库（等价于 RocketMQ half message），messageId={}", messageId);

        try {
            transactionTemplate.executeWithoutResult(tx -> {
                jdbcTemplate.update(
                        "INSERT INTO t_payment_order (order_no, user_id, amount, status) VALUES (?, ?, ?, 'PAID')",
                        orderNo, userId, amount);
                if (fail) {
                    throw new IllegalStateException("模拟本地事务执行失败");
                }
            });
            log.info("[TxMsg] Phase 2：本地事务提交成功，orderNo={}", orderNo);
        } catch (RuntimeException e) {
            jdbcTemplate.update("UPDATE t_tx_message SET status = ? WHERE message_id = ?",
                    STATUS_ROLLBACK, messageId);
            log.error("[TxMsg] 本地事务回滚，半消息标记为已回滚，消息不会投递，messageId={}", messageId, e);
            return;
        }

        jdbcTemplate.update("UPDATE t_tx_message SET status = ? WHERE message_id = ?",
                STATUS_COMMITTED, messageId);
        log.info("[TxMsg] Phase 3：半消息提交为待发送，开始投递，messageId={}", messageId);
        send(messageId, message);
    }

    public void payHalfOnly(String orderNo, Long userId, int points) {
        String messageId = UUID.randomUUID().toString();
        PointsMessage message = new PointsMessage(orderNo, userId, points);
        String payload = objectMapper.writeValueAsString(message);

        jdbcTemplate.update(
                "INSERT INTO t_tx_message (message_id, biz_key, exchange, routing_key, payload, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                messageId, orderNo, RabbitTxMessageConfig.POINTS_EXCHANGE,
                RabbitTxMessageConfig.POINTS_ROUTING_KEY, payload, STATUS_HALF);
        log.warn("[TxMsg] 模拟进程在本地事务执行前崩溃：仅半消息落库，无订单数据，messageId={}，等待回查", messageId);
    }

    public boolean orderExists(String bizKey) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM t_payment_order WHERE order_no = ?", Integer.class, bizKey);
        return count != null && count > 0;
    }

    public void markCommittedAndSend(String messageId, String payload) {
        PointsMessage message = objectMapper.readValue(payload, PointsMessage.class);
        jdbcTemplate.update("UPDATE t_tx_message SET status = ? WHERE message_id = ?",
                STATUS_COMMITTED, messageId);
        send(messageId, message);
    }

    public void markRolledBack(String messageId) {
        jdbcTemplate.update("UPDATE t_tx_message SET status = ? WHERE message_id = ?",
                STATUS_ROLLBACK, messageId);
    }

    public void send(String messageId, PointsMessage message) {
        try {
            rabbitTemplate.convertAndSend(RabbitTxMessageConfig.POINTS_EXCHANGE,
                    RabbitTxMessageConfig.POINTS_ROUTING_KEY, message,
                    new CorrelationData(messageId));
            log.info("[TxMsg] 消息已发出，等待 Broker Confirm，messageId={}", messageId);
        } catch (Exception e) {
            log.error("[TxMsg] 发送异常，状态保持已提交，等待兜底重发，messageId={}", messageId, e);
        }
    }
}
