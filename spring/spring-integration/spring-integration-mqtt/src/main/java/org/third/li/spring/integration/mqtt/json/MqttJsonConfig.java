package org.third.li.spring.integration.mqtt.json;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.MessageBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 示例 6：JSON 载荷。
 *
 * <p>MQTT 载荷本质是字节流；对象→JSON 的序列化用 Jackson 在业务侧完成
 * （spring-integration-mqtt 6.3.x 的 DefaultPahoMessageConverter 不提供
 * PayloadType.JSON 枚举，需要自行序列化后以 String 载荷发布）。
 * 入站同理可自行反序列化。对照 mq 组 RocketMQ/RabbitMQ 的消息转换器。
 */
@Configuration
public class MqttJsonConfig {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    /** 出站指令（序列化源对象）。 */
    public record DeviceCommand(String deviceId, String command, LocalDateTime issuedAt) {
    }

    @Value("${mqtt.client-id-prefix:demo}")
    private String clientIdPrefix;

    @Bean
    public MessageChannel mqttJsonOutboundChannel() {
        return new DirectChannel();
    }

    /** 出站适配器：发布 JSON 字符串。 */
    @Bean
    public MessageHandler mqttJsonOutbound(MqttPahoClientFactory factory) {
        MqttPahoMessageHandler handler =
                new MqttPahoMessageHandler(clientIdPrefix + "-json-out", factory);
        handler.setDefaultTopic("demo/device/command");
        handler.setDefaultQos(1);
        handler.setAsync(true);
        return handler;
    }

    /** 发布 JSON 指令：Jackson 序列化 POJO 后以 String 载荷发布。 */
    public void publish(DeviceCommand command) {
        try {
            String json = MAPPER.writeValueAsString(command);
            mqttJsonOutboundChannel().send(MessageBuilder.withPayload(json).build());
        } catch (Exception e) {
            throw new IllegalStateException("序列化失败", e);
        }
    }
}
