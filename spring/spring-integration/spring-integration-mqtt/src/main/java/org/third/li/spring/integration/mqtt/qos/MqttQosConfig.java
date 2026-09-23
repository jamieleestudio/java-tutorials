package org.third.li.spring.integration.mqtt.qos;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

/**
 * 示例 5：QoS 等级对比。
 *
 * <ul>
 *   <li><b>QoS 0</b>（最多一次）：fire-and-forget，可能丢失，开销最小</li>
 *   <li><b>QoS 1</b>（至少一次）：PUBACK 确认，可能重复，最常用</li>
 *   <li><b>QoS 2</b>（恰好一次）：四步握手，不丢不重，开销最大</li>
 * </ul>
 * 三个出站适配器分别以不同 QoS 发布，REST 可对比发送耗时与到达情况。
 */
@Configuration
public class MqttQosConfig {

    @Value("${mqtt.client-id-prefix:demo}")
    private String clientIdPrefix;

    @Bean
    public MessageChannel mqttQos0Channel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel mqttQos1Channel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel mqttQos2Channel() {
        return new DirectChannel();
    }

    @Bean
    public MessageHandler qos0Handler(MqttPahoClientFactory factory) {
        MqttPahoMessageHandler handler =
                new MqttPahoMessageHandler(clientIdPrefix + "-qos0", factory);
        handler.setDefaultTopic("demo/qos/0");
        handler.setDefaultQos(0);
        handler.setAsync(true);
        return handler;
    }

    @Bean
    public MessageHandler qos1Handler(MqttPahoClientFactory factory) {
        MqttPahoMessageHandler handler =
                new MqttPahoMessageHandler(clientIdPrefix + "-qos1", factory);
        handler.setDefaultTopic("demo/qos/1");
        handler.setDefaultQos(1);
        handler.setAsync(true);
        return handler;
    }

    @Bean
    public MessageHandler qos2Handler(MqttPahoClientFactory factory) {
        MqttPahoMessageHandler handler =
                new MqttPahoMessageHandler(clientIdPrefix + "-qos2", factory);
        handler.setDefaultTopic("demo/qos/2");
        handler.setDefaultQos(2);
        handler.setAsync(true);
        return handler;
    }
}
