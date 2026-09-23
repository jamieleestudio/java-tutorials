package org.third.li.spring.integration.mqtt.controller;

import java.time.LocalDateTime;

import org.third.li.spring.integration.mqtt.inbound.MqttInboundConfig;
import org.third.li.spring.integration.mqtt.json.MqttJsonConfig;
import org.third.li.spring.integration.mqtt.json.MqttJsonConfig.DeviceCommand;
import org.third.li.spring.integration.mqtt.outbound.MqttOutboundConfig;
import org.third.li.spring.integration.mqtt.v5.MqttV5SharedSubscriptionConfig;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * MQTT 示例 REST 入口：触发发布、QoS 对比、JSON 载荷、v5 共享订阅、查看接收消息。
 */
@RestController
@RequestMapping("/mqtt")
public class MqttDemoController {

    private final MqttOutboundConfig.MqttOutboundGateway outboundGateway;
    private final MqttInboundConfig inboundConfig;
    private final MqttJsonConfig jsonConfig;
    private final MqttV5SharedSubscriptionConfig v5Config;

    public MqttDemoController(
            MqttOutboundConfig.MqttOutboundGateway outboundGateway,
            MqttInboundConfig inboundConfig,
            MqttJsonConfig jsonConfig,
            MqttV5SharedSubscriptionConfig v5Config) {
        this.outboundGateway = outboundGateway;
        this.inboundConfig = inboundConfig;
        this.jsonConfig = jsonConfig;
        this.v5Config = v5Config;
    }

    /** 示例 2：发布消息到指定 topic（QoS 1）。 */
    @PostMapping("/publish")
    public String publish(
            @RequestParam(defaultValue = "demo/default") String topic,
            @RequestParam(defaultValue = "hello mqtt") String payload) {
        outboundGateway.publish(topic, payload);
        return "已发布到 " + topic + "：" + payload;
    }

    /** 示例 3/4：查看已接收消息（自发自收 + 外部发布）。 */
    @GetMapping("/received")
    public Object received() {
        return inboundConfig.receivedMessages();
    }

    /** 示例 6：发布 JSON 载荷（DeviceCommand 自动序列化）。 */
    @PostMapping("/json")
    public String publishJson(@RequestParam(defaultValue = "device-001") String deviceId) {
        DeviceCommand command =
                new DeviceCommand(deviceId, "restart", LocalDateTime.now());
        jsonConfig.publish(command);
        return "JSON 指令已发布：" + command;
    }

    /** 示例 7：v5 共享订阅——发布一条任务并查看两个消费者的分担记录。 */
    @PostMapping("/v5/task")
    public Object v5Task(@RequestParam(defaultValue = "任务-A") String task) {
        v5Config.publishTask(task + " @ " + LocalDateTime.now());
        return v5Config.taskLog();
    }

    /** v5 任务记录。 */
    @GetMapping("/v5/task/log")
    public Object v5TaskLog() {
        return (Object) v5Config.taskLog();
    }
}
