package com.yineng.bpe.rabbit.txmessage.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class RabbitTxMessageConfig {

    public static final String POINTS_EXCHANGE = "points.txmsg.exchange";
    public static final String POINTS_QUEUE = "points.txmsg.queue";
    public static final String POINTS_ROUTING_KEY = "points.add";

    private static final Logger log = LoggerFactory.getLogger(RabbitTxMessageConfig.class);

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
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         JacksonJsonMessageConverter converter,
                                         JdbcTemplate jdbcTemplate) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        template.setMandatory(true);

        template.setConfirmCallback((correlationData, ack, cause) -> {
            String messageId = correlationData == null ? "unknown" : correlationData.getId();
            if (ack) {
                int rows = jdbcTemplate.update(
                        "UPDATE t_tx_message SET status = 3 WHERE message_id = ? AND status = 1", messageId);
                if (rows > 0) {
                    log.info("[Confirm] 消息已到达 Broker，标记为已送达，messageId={}", messageId);
                }
            } else {
                log.error("[Confirm] 消息未到达 Broker，等待兜底重发，messageId={}, cause={}", messageId, cause);
            }
        });

        template.setReturnsCallback(returned -> log.error(
                "[Return] 消息无法路由到队列，已退回，exchange={}, routingKey={}, replyText={}",
                returned.getExchange(), returned.getRoutingKey(), returned.getReplyText()));

        return template;
    }
}
