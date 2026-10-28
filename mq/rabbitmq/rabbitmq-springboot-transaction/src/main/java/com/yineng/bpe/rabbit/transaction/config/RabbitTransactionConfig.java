package com.yineng.bpe.rabbit.transaction.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitTransactionConfig {

    public static final String POINTS_EXCHANGE = "points.tx.exchange";
    public static final String POINTS_QUEUE = "points.tx.queue";
    public static final String POINTS_ROUTING_KEY = "points.add";

    @Bean
    public DirectExchange pointsExchange() {
        return new DirectExchange(POINTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue pointsQueue() {
        return new Queue(POINTS_QUEUE, true);
    }

    @Bean
    public Binding pointsBinding() {
        return BindingBuilder.bind(pointsQueue()).to(pointsExchange()).with(POINTS_ROUTING_KEY);
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory,
                                         JacksonJsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        template.setChannelTransacted(true);
        return template;
    }
}
