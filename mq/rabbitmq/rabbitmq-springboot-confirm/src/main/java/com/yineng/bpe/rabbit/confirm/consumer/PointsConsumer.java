package com.yineng.bpe.rabbit.confirm.consumer;

import com.yineng.bpe.rabbit.confirm.config.RabbitConfirmConfig;
import com.yineng.bpe.rabbit.confirm.message.PointsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PointsConsumer {

    private static final Logger log = LoggerFactory.getLogger(PointsConsumer.class);

    @RabbitListener(queues = RabbitConfirmConfig.POINTS_QUEUE)
    public void onMessage(PointsMessage message) {
        log.info("[Consumer] 收到积分消息，orderNo={}, userId={}, points={}",
                message.orderNo(), message.userId(), message.points());
    }
}
