package com.yineng.bpe.rocketmq.api.common;

import com.yineng.bpe.rocketmq.api.util.RocketmqUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class SimpleConsumer {
    public static void main(String[] args) throws MQClientException {
        // Instantiate with specified consumer group name.
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("my_test_consumer_group");

        // Specify name server addresses.
        consumer.setNamesrvAddr(RocketmqUtils.getNamesrvAddr());
        consumer.setMessageModel(MessageModel.BROADCASTING);

        // Subscribe one more more topics to consume.
        consumer.subscribe("q-2-1", "*");
        // Register callback to execute on arrival of messages fetched from brokers.
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                            ConsumeConcurrentlyContext context) {
                System.out.printf("%s Receive New Messages: %s %n", Thread.currentThread().getName(), msgs);

                for(MessageExt msg : msgs){
                    String topic = msg.getTopic();
                    String messageBody="";
                    try {
                        messageBody = new String(msg.getBody(),"utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        // 重新消费
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }
                    String tags = msg.getTags();
                    System.out.println("topic:"+topic+",tags:"+tags+",msg:"+messageBody);
                }

                // 消费成功
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        //Launch the consumer instance.
        consumer.start();

        System.out.printf("Consumer Started.%n");
    }
}
