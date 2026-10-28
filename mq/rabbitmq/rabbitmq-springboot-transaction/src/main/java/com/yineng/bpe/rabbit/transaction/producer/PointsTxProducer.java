package com.yineng.bpe.rabbit.transaction.producer;

import tools.jackson.databind.ObjectMapper;
import com.yineng.bpe.rabbit.transaction.config.RabbitTransactionConfig;
import com.yineng.bpe.rabbit.transaction.message.PointsMessage;
import com.rabbitmq.client.AMQP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PointsTxProducer {

    private static final Logger log = LoggerFactory.getLogger(PointsTxProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public PointsTxProducer(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendInTransaction(String orderNo, Long userId, int points) throws IOException {
        PointsMessage message = new PointsMessage(orderNo, userId, points);
        byte[] body = toJson(message);

        rabbitTemplate.execute(channel -> {
            try {
                channel.txSelect();
                log.info("[Tx] 开启 AMQP 事务，body={}", message);
                channel.basicPublish(RabbitTransactionConfig.POINTS_EXCHANGE,
                        RabbitTransactionConfig.POINTS_ROUTING_KEY,
                        buildPersistentJsonProperties(), body);
                channel.txCommit();
                log.info("[Tx] txCommit 成功，消息已确认写入 Broker");
            } catch (IOException e) {
                channel.txRollback();
                log.error("[Tx] 发送异常，txRollback 回滚，消息未投递", e);
                throw e;
            }
            return null;
        });
    }

    public void sendThenFail(String orderNo, Long userId, int points) throws IOException {
        PointsMessage message = new PointsMessage(orderNo, userId, points);
        byte[] body = toJson(message);

        rabbitTemplate.execute(channel -> {
            channel.txSelect();
            log.info("[Tx] 开启 AMQP 事务，body={}，随后模拟业务异常", message);
            channel.basicPublish(RabbitTransactionConfig.POINTS_EXCHANGE,
                    RabbitTransactionConfig.POINTS_ROUTING_KEY,
                    buildPersistentJsonProperties(), body);
            try {
                simulateBusinessFailure();
                channel.txCommit();
                log.info("[Tx] txCommit 成功");
            } catch (RuntimeException e) {
                channel.txRollback();
                log.error("[Tx] 业务异常，txRollback 回滚，消息不会到达消费者", e);
            }
            return null;
        });
    }

    private void simulateBusinessFailure() {
        throw new IllegalStateException("模拟业务处理失败");
    }

    private AMQP.BasicProperties buildPersistentJsonProperties() {
        return new AMQP.BasicProperties.Builder()
                .contentType("application/json")
                .deliveryMode(2)
                .build();
    }

    private byte[] toJson(PointsMessage message) {
        return objectMapper.writeValueAsBytes(message);
    }
}
