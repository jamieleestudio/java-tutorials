package org.third.li.spring.integration.mqtt.outbound;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

/**
 * 示例 2：消息发布（出站）。
 *
 * <p>两层结构：
 * <ol>
 *   <li>{@code MqttPahoMessageHandler}：出站适配器，绑定默认 topic 与 QoS，
 *       挂在 {@code mqttOutboundChannel} 上</li>
 *   <li>{@code @MessagingGateway}：同步网关接口，调用方法即向通道发消息，
 *       支持 @Header 动态指定 topic（覆盖默认值）</li>
 * </ol>
 * 业务代码只需调用 {@link MqttOutboundGateway#publish(String, String)}，
 * 与具体 MQTT 客户端解耦。
 */
@Configuration
public class MqttOutboundConfig {

    public static final String CHANNEL_MQTT_OUT = "mqttOutboundChannel";

    @Value("${mqtt.client-id-prefix:demo}")
    private String clientIdPrefix;

    /** 出站通道：向此通道发送 Message 即发布到 MQTT。 */
    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    /** 出站适配器：默认 topic demo/default，QoS 1，消息写入后由 Paho 客户端发布。 */
    @Bean
    public MessageHandler mqttOutbound(MqttPahoClientFactory mqttClientFactory) {
        MqttPahoMessageHandler handler =
                new MqttPahoMessageHandler(clientIdPrefix + "-outbound", mqttClientFactory);
        handler.setAsync(true);
        handler.setDefaultTopic("demo/default");
        handler.setDefaultQos(1);
        return handler;
    }

    /** 同步网关：业务代码调用接口方法即发布消息。 */
    @MessagingGateway(defaultRequestChannel = CHANNEL_MQTT_OUT)
    public interface MqttOutboundGateway {

        /**
         * @param topic   目标 topic（经 @Header 覆盖适配器默认值）
         * @param payload 消息载荷
         */
        void publish(@Header(MqttHeaders.TOPIC) String topic, @Payload String payload);
    }
}
