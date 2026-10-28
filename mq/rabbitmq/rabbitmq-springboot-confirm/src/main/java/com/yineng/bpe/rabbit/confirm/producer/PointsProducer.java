package com.yineng.bpe.rabbit.confirm.producer;

import com.yineng.bpe.rabbit.confirm.config.RabbitConfirmConfig;
import com.yineng.bpe.rabbit.confirm.message.PointsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PointsProducer {

    private static final Logger log = LoggerFactory.getLogger(PointsProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public PointsProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendPointsMessage(String orderNo, Long userId, int points) {
        PointsMessage message = new PointsMessage(orderNo, userId, points);
        String messageId = UUID.randomUUID().toString();
        log.info("[Producer] 发送积分消息，messageId={}, body={}", messageId, message);
        rabbitTemplate.convertAndSend(RabbitConfirmConfig.POINTS_EXCHANGE,
                RabbitConfirmConfig.POINTS_ROUTING_KEY, message, m -> {
                    m.getMessageProperties().setMessageId(messageId);
                    return m;
                }, new org.springframework.amqp.rabbit.connection.CorrelationData(messageId));
    }

    public void sendUnroutableMessage(String orderNo, Long userId, int points) {
        PointsMessage message = new PointsMessage(orderNo, userId, points);
        String messageId = UUID.randomUUID().toString();
        log.info("[Producer] 发送不可路由消息（演示 Return），messageId={}, body={}", messageId, message);
        rabbitTemplate.convertAndSend(RabbitConfirmConfig.POINTS_EXCHANGE,
                "points.not-exist", message, m -> {
                    m.getMessageProperties().setMessageId(messageId);
                    return m;
                }, new org.springframework.amqp.rabbit.connection.CorrelationData(messageId));
    }
}
