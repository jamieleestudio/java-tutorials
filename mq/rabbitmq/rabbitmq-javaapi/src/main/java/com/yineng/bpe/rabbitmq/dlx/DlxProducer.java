package com.yineng.bpe.rabbitmq.dlx;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.yineng.bpe.rabbitmq.util.RabbitmqConnectionUtils;

/**
 * 消息生产者，通过TTL测试死信队列
 */
public class DlxProducer {

    public static void main(String[] args) throws Exception {
        Connection conn = RabbitmqConnectionUtils.getConnection();
        // 创建消息通道
        Channel channel = conn.createChannel();

        String msg = "Hello world, Rabbit MQ, DLX MSG";

        // 设置属性，消息10秒钟过期
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                .deliveryMode(2) // 持久化消息
                .contentEncoding("UTF-8")
                .expiration("10000") // TTL
                .build();

        // 发送消息
        channel.basicPublish("", "GP_ORI_USE_QUEUE", properties, msg.getBytes());

        channel.close();
        conn.close();
    }
}

