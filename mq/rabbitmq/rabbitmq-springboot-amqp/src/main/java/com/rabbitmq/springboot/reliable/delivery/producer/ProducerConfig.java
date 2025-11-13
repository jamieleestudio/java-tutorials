package com.rabbitmq.springboot.reliable.delivery.producer;

import com.rabbitmq.springboot.util.ResourceUtil;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.UnsupportedEncodingException;

/**
 * 消息投递的可靠性保证——Confirm和Return
 */
@Configuration
public class ProducerConfig {

    @Bean
    public ConnectionFactory connectionFactory() throws Exception {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setUri(ResourceUtil.getKey("rabbitmq.uri"));

        // 生产者确认
        cachingConnectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.SIMPLE);
        // 无法路由的消息返回
        cachingConnectionFactory.setPublisherReturns(true);

        return cachingConnectionFactory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        // 消息是否必须路由到一个队列中，配合Return使用
        rabbitTemplate.setMandatory(true);
        // 为RabbitTemplate设置ReturnCallback
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            @Override
            public void returnedMessage(ReturnedMessage returned) {
                try {
                    System.out.println("--------收到无法路由回发的消息--------");
                    System.out.println("replyCode:" +  returned.getReplyCode());
                    System.out.println("replyText:" + returned.getReplyText());
                    System.out.println("exchange:" + returned.getExchange());
                    System.out.println("routingKey:" + returned.getRoutingKey());
                    System.out.println("body:" + new String(returned.getMessage().getBody(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

        // Confirm异步确认，收到服务端的ACK以后会调用
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("--------收到服务端异步确认--------");
                System.out.println("received ack: "+ack);
                System.out.println("cause: "+cause);
                System.out.println("correlationId: "+correlationData.getId());
            }
        });

        return rabbitTemplate;
    }


}
