package org.third.li.spring.integration.mqtt.inbound;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Payload;

/**
 * 示例 3 + 4：订阅消费与 Topic 通配符。
 *
 * <p>{@code MqttPahoMessageDrivenChannelAdapter} 订阅多个 topic（含通配符）：
 * <ul>
 *   <li>{@code demo/data/+}：单层通配符，匹配 demo/data/temp、demo/data/humidity 等</li>
 *   <li>{@code demo/log/#}：多层通配符，匹配 demo/log 下所有层级</li>
 * </ul>
 * 收到的消息经 {@code @ServiceActivator} 汇入内存列表，供 REST 查看与自发自收验证。
 */
@Configuration
public class MqttInboundConfig {

    /** 已接收消息的内存快照（演示用；生产应落库或转发下游）。 */
    private final List<String> receivedMessages = new CopyOnWriteArrayList<>();

    @Value("${mqtt.client-id-prefix:demo}")
    private String clientIdPrefix;

    @Bean
    public MessageChannel mqttInboundChannel() {
        return new DirectChannel();
    }

    /** 入站适配器：订阅通配符 topic，消息转换为 String 后流入 mqttInboundChannel。 */
    @Bean
    public MessageProducerSupport mqttInboundAdapter(MqttPahoClientFactory mqttClientFactory) {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        clientIdPrefix + "-inbound", mqttClientFactory,
                        "demo/data/+", "demo/log/#");
        adapter.setCompletionTimeout(10_000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInboundChannel());
        return adapter;
    }

    /** 服务激活器：处理每条到达的消息。 */
    @org.springframework.integration.annotation.ServiceActivator(inputChannel = "mqttInboundChannel")
    public void handleMqttMessage(@Payload String payload) {
        String record = "[%s] %s".formatted(java.time.LocalTime.now(), payload);
        receivedMessages.add(record);
    }

    /** REST 查看已接收消息。 */
    public List<String> receivedMessages() {
        return List.copyOf(receivedMessages);
    }
}
