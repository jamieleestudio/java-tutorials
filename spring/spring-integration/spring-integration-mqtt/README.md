# Spring Integration MQTT 示例

用 **Spring Integration 6.3（Boot 3.3.3 BOM 管理）+ Eclipse Paho v3/v5** 演示
Spring 生态下 MQTT 的接入与使用：连接工厂、出站发布、入站订阅、Topic 通配符、
QoS 对比、JSON 载荷、v5 共享订阅。

> MQTT 是 IoT 场景的轻量发布/订阅协议（与 mq 组的 RocketMQ/RabbitMQ 企业消息队列定位不同）。
> Spring 侧标准接入方式是 **Spring Integration MQTT** 的通道适配器（Channel Adapter），
> 业务代码通过 MessageChannel / @MessagingGateway 与 MQTT 解耦。

## 运行方式

```bash
# 1. 启动本地 Mosquitto broker
docker run -d -p 1883:1883 eclipse-mosquitto:2

# 2. 启动本模块（端口 8090）
cd spring/spring-integration/spring-integration-mqtt
mvn spring-boot:run
```

## 示例清单（7 个主题）

| # | 示例 | 触发方式 | 核心类 |
|---|---|---|---|
| 1 | 连接工厂 | 启动即生效 | `MqttClientFactoryConfig`（broker/凭据/自动重连） |
| 2 | 消息发布 | `POST /mqtt/publish?topic=&payload=` | `MqttOutboundConfig`（@MessagingGateway 出站网关） |
| 3 | 订阅消费 | `GET /mqtt/received` | `MqttInboundConfig`（MessageDrivenChannelAdapter + @ServiceActivator） |
| 4 | Topic 通配符 | 同上 | `+` 单层 / `#` 多层订阅 |
| 5 | QoS 对比 | `POST /mqtt/qos?level=&payload=` | 三个出站适配器（0/1/2） |
| 6 | JSON 载荷 | `POST /mqtt/json?deviceId=` | Jackson 序列化 POJO → String 载荷 |
| 7 | v5 共享订阅 | `POST /mqtt/v5/task` → `GET /mqtt/v5/task/log` | `Mqttv5Paho*` + `$share/demo-group` 负载分担 |

## 技术要点

- **入站**：`MqttPahoMessageDrivenChannelAdapter` 订阅 topic（支持通配符），消息经
  converter 转换后流入 channel，`@ServiceActivator` 处理
- **出站**：`MqttPahoMessageHandler` 挂在 channel 上，`@MessagingGateway` 提供
  类型安全的发布接口，`@Header(MqttHeaders.TOPIC)` 动态指定 topic
- **QoS**：0 最多一次 / 1 至少一次（默认）/ 2 恰好一次，按业务可靠性要求选择
- **v5 共享订阅**：`$share/<group>/<filter>`，broker 组内负载分担；
  注意 `piiRedactionMiddleware` 之外，MQTT v5 的 Shared Subscription 需要
  broker 支持（Mosquitto 2.x / EMQX 均支持）
- **JSON**：spring-integration-mqtt 6.3.x 的 DefaultPahoMessageConverter 不提供
  PayloadType.JSON 枚举，对象→JSON 的序列化用 Jackson 在业务侧完成后以 String 载荷发布
