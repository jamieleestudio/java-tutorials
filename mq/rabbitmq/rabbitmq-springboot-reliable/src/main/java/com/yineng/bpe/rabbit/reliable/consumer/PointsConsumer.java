package com.yineng.bpe.rabbit.reliable.consumer;

import com.yineng.bpe.rabbit.reliable.config.RabbitReliableConfig;
import com.yineng.bpe.rabbit.reliable.message.PointsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PointsConsumer {

    private static final Logger log = LoggerFactory.getLogger(PointsConsumer.class);

    private final JdbcTemplate jdbcTemplate;

    public PointsConsumer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @RabbitListener(queues = RabbitReliableConfig.POINTS_QUEUE)
    public void onMessage(PointsMessage message) {
        int rows = jdbcTemplate.update(
                "INSERT IGNORE INTO t_points_record (order_no, user_id, points) VALUES (?, ?, ?)",
                message.orderNo(), message.userId(), message.points());
        if (rows == 0) {
            log.warn("[Consumer] 重复消息，幂等拦截，orderNo={}", message.orderNo());
        } else {
            log.info("[Consumer] 积分发放成功，orderNo={}, userId={}, points={}",
                    message.orderNo(), message.userId(), message.points());
        }
    }
}
