# rabbitmq-springboot-confirm — 方案1：Publisher Confirm 确认机制

## 做什么

演示 RabbitMQ 的 **Publisher Confirm + Return** 机制：消息发出后，生产者如何异步感知「消息是否真的到达了 Broker」。解决的是 **生产者 → Broker** 这一段的消息不丢问题。

## 核心机制

```
convertAndSend() ──→ Broker 持久化成功 ──→ ConfirmCallback(ack=true)
                └─→ Broker 持久化失败 ──→ ConfirmCallback(ack=false, cause)
                └─→ 到达 Broker 但路由不到队列 ──→ ReturnsCallback(replyCode=312)
```

- `publisher-confirm-type: correlated`：开启 Confirm 模式，异步回调并携带 `CorrelationData`
- `mandatory: true`：消息无法路由时不静默丢弃，退回给生产者（触发 Return）
- **Confirm 与 Return 是两回事**：ack=true 只代表消息到达了 Broker（进了交换机），不代表路由到了队列；路由失败由 Return 单独告知

## 关键代码

| 文件 | 内容 |
|---|---|
| `config/RabbitConfirmConfig.java` | Confirm/Return 回调注册、JSON 转换器、交换机/队列/绑定声明 |
| `producer/PointsProducer.java` | 发送时携带 `CorrelationData(messageId)`，回调时凭它识别是哪条消息 |
| `consumer/PointsConsumer.java` | 打印收到的消息，验证链路 |
| `controller/PaymentController.java` | HTTP 触发入口 |

## 运行验证

```bash
# 1. 启动依赖（RabbitMQ 5672/15672 admin/admin）
cd mq/rabbitmq/docker && docker compose up -d

# 2. 启动应用（端口 18081）
cd mq/rabbitmq && mvn spring-boot:run -pl rabbitmq-springboot-confirm

# 3. 正常发送 → Confirm ack + 消费者收到
curl -X POST "localhost:18081/pay?orderNo=C001&userId=3001&points=100"

# 4. 不可路由消息 → 触发 Return，但 Confirm 依然 ack
curl -X POST "localhost:18081/pay/unroutable?orderNo=C002&userId=3002&points=100"
```

预期日志：

```
[Confirm] 消息已到达 Broker，messageId=fe04...
[Consumer] 收到积分消息，orderNo=C001 ...
[Return] 消息无法路由到队列，已退回，exchange=points.confirm.exchange,
         routingKey=points.not-exist, replyCode=312, replyText=NO_ROUTE ...
[Confirm] 消息已到达 Broker，messageId=3406...     ← 注意：Return 了但 ack 仍是 true
```

## 适用场景与局限

- **适合**：日志、通知类等「允许极小概率丢失」的场景；或作为方案3/4 的底层投递确认
- **局限**：只保证「已发出的消息到达 Broker」。业务写库与发消息之间的一致性（先发后崩、先崩后发）它管不了——需要方案3（落库+补偿）
