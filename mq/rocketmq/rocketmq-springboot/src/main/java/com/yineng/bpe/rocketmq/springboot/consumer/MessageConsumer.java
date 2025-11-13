package com.yineng.bpe.rocketmq.springboot.consumer;

import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * MessageModel：集群模式；广播模式
 * ConsumeMode：顺序消费；无序消费
 */
@Component
@RocketMQMessageListener(topic = "springboot-topic",consumerGroup = "qs-consumer-group",
        //selectorExpression = "tag1",selectorType = SelectorType.TAG,
        messageModel = MessageModel.CLUSTERING, consumeMode = ConsumeMode.CONCURRENTLY)
public class MessageConsumer implements RocketMQListener<String> {
    @Override
    public void onMessage(String message) {
        try {
            System.out.println("----------接收到rocketmq消息:" + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
