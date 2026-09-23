package org.third.li.spring.integration.mqtt.v5;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.inbound.Mqttv5PahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.Mqttv5PahoMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Payload;

/**
 * 示例 7：MQTT v5 共享订阅（Shared Subscriptions）。
 *
 * <p>v5 独有特性：多个消费者以 {@code $share/<group>/<filter>} 订阅同一 topic，
 * broker 将消息在组内负载分担（每条消息只投递给组内一个消费者）——
 * 天然实现消费端水平扩展，无需应用层做消息分发。
 *
 * <p>本示例启动两个 v5 适配器加入同一共享组，模拟两个消费者实例；
 * REST 触发任务发布后，可在任务日志中观察两个消费者的负载分担情况。
 */
@Configuration
public class MqttV5SharedSubscriptionConfig {

    public static final String SHARED_TOPIC = "$share/demo-group/demo/v5/task";
    public static final String PUBLISH_TOPIC = "demo/v5/task";

    private final List<String> taskLog = new CopyOnWriteArrayList<>();

    @Value("${mqtt.broker-url:tcp://localhost:1883}")
    private String brokerUrl;

    @Bean
    public MqttConnectionOptions mqttv5ConnectOptions() {
        MqttConnectionOptions options = new MqttConnectionOptions();
        options.setServerURIs(new String[] { brokerUrl });
        options.setAutomaticReconnect(true);
        options.setSessionExpiryInterval(60L);
        return options;
    }

    @Bean
    public MessageChannel mqttV5Consumer1Channel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel mqttV5Consumer2Channel() {
        return new DirectChannel();
    }

    /** 消费者 1（共享组 demo-group）。 */
    @Bean
    public Mqttv5PahoMessageDrivenChannelAdapter mqttV5Consumer1(MqttConnectionOptions options) {
        var adapter = new Mqttv5PahoMessageDrivenChannelAdapter(
                options, "v5-consumer-1",
                new MqttSubscription(SHARED_TOPIC, 1));
        adapter.setOutputChannel(mqttV5Consumer1Channel());
        return adapter;
    }

    /** 消费者 2（共享组 demo-group）。 */
    @Bean
    public Mqttv5PahoMessageDrivenChannelAdapter mqttV5Consumer2(MqttConnectionOptions options) {
        var adapter = new Mqttv5PahoMessageDrivenChannelAdapter(
                options, "v5-consumer-2",
                new MqttSubscription(SHARED_TOPIC, 1));
        adapter.setOutputChannel(mqttV5Consumer2Channel());
        return adapter;
    }

    @ServiceActivator(inputChannel = "mqttV5Consumer1Channel")
    public void handleByConsumer1(@Payload String payload) {
        taskLog.add("[consumer-1] " + payload);
    }

    @ServiceActivator(inputChannel = "mqttV5Consumer2Channel")
    public void handleByConsumer2(@Payload String payload) {
        taskLog.add("[consumer-2] " + payload);
    }

    /** v5 出站适配器（发布任务到共享 topic）。 */
    @Bean
    public MessageHandler mqttV5Outbound(MqttConnectionOptions options) {
        Mqttv5PahoMessageHandler handler =
                new Mqttv5PahoMessageHandler(options, "v5-publisher");
        handler.setDefaultTopic(PUBLISH_TOPIC);
        handler.setDefaultQos(1);
        return handler;
    }

    /** 发布任务：向共享 topic 的出站适配器发送。 */
    public void publishTask(String task) {
        MessageHandler outbound = mqttV5Outbound(mqttv5ConnectOptions());
        outbound.handleMessage(MessageBuilder.withPayload(task).build());
    }

    /** 查看共享组分发的任务日志。 */
    public List<String> taskLog() {
        return List.copyOf(taskLog);
    }
}
