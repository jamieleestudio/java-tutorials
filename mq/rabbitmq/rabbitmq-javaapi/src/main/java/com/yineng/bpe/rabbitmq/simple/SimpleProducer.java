package com.yineng.bpe.rabbitmq.simple;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.yineng.bpe.rabbitmq.util.RabbitmqConnectionUtils;


public class SimpleProducer {


    private final static String EXCHANGE_NAME = "SIMPLE_EXCHANGE";

    public static void main(String[] args) throws Exception {

        Connection conn = RabbitmqConnectionUtils.getConnection();
        // 创建消息通道
        Channel channel = conn.createChannel();

        // 发送消息
        String msg = "Hello world, Rabbit MQ";

        // String exchange, String routingKey, BasicProperties props, byte[] body
        channel.basicPublish(EXCHANGE_NAME, "simple.best", null, msg.getBytes());

        channel.close();
        conn.close();
    }
}

