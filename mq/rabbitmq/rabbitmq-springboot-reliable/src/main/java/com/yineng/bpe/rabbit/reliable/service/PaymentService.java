package com.yineng.bpe.rabbit.reliable.service;

import com.yineng.bpe.rabbit.reliable.config.RabbitReliableConfig;
import com.yineng.bpe.rabbit.reliable.message.PointsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    public PaymentService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper, RabbitTemplate rabbitTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public void pay(String orderNo, Long userId, BigDecimal amount, int points) {
        jdbcTemplate.update(
                "INSERT IGNORE INTO t_payment_order (order_no, user_id, amount, status) VALUES (?, ?, ?, 'PAID')",
                orderNo, userId, amount);

        String messageId = UUID.randomUUID().toString();
        PointsMessage message = new PointsMessage(orderNo, userId, points);
        String payload = objectMapper.writeValueAsString(message);

        jdbcTemplate.update(
                "INSERT INTO t_local_message (message_id, biz_key, exchange, routing_key, payload, status, next_retry_time) "
                        + "VALUES (?, ?, ?, ?, ?, 0, ?)",
                messageId, orderNo, RabbitReliableConfig.POINTS_EXCHANGE,
                RabbitReliableConfig.POINTS_ROUTING_KEY, payload, LocalDateTime.now().plusSeconds(30));

        log.info("[Pay] 本地事务提交：订单落库 + 消息落库（待发送），orderNo={}, messageId={}", orderNo, messageId);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("[Pay] 事务已提交，开始发送消息，messageId={}", messageId);
                send(messageId, message);
            }
        });
    }

    public void send(String messageId, PointsMessage message) {
        try {
            rabbitTemplate.convertAndSend(RabbitReliableConfig.POINTS_EXCHANGE,
                    RabbitReliableConfig.POINTS_ROUTING_KEY, message,
                    new CorrelationData(messageId));
            log.info("[Send] 消息已发出，等待 Broker Confirm，messageId={}", messageId);
        } catch (Exception e) {
            log.error("[Send] 发送异常，状态保持待发送，等待定时补偿，messageId={}", messageId, e);
        }
    }

    public void resend(String messageId, String payload) {
        try {
            PointsMessage message = objectMapper.readValue(payload, PointsMessage.class);
            send(messageId, message);
        } catch (Exception e) {
            log.error("[Compensate] 补偿重发失败，messageId={}", messageId, e);
        }
    }
}
