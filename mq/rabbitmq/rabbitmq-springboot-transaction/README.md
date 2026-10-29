# rabbitmq-springboot-transaction — 方案2：AMQP 事务机制

## 做什么

演示 RabbitMQ 原生 AMQP 事务：`channel.txSelect()` / `txCommit()` / `txRollback()`，以及「业务异常时回滚已发出的消息」。

## 核心机制

```
txSelect()                     开启事务模式
basicPublish(...)              消息进入事务缓冲，Broker 暂不投递
txCommit()                     提交：Broker 确认写入并投递
txRollback()                   回滚：缓冲中的消息全部丢弃，消费者永远收不到
```

## 关键代码

| 文件 | 内容 |
|---|---|
| `producer/PointsTxProducer.java` | `rabbitTemplate.execute(channel -> ...)` 中直接操作 Channel：提交链路 + 业务异常回滚链路 |
| `config/RabbitTransactionConfig.java` | `template.setChannelTransacted(true)`（关键，见下方坑） |

**踩坑记录**：Spring 的 `CachingConnectionFactory` 代理的 Channel 默认非事务模式，直接 `txSelect()` 会抛
`Cannot start transaction on non-transactional channel`。必须给 `RabbitTemplate` 设置
`channelTransacted=true`，让它取 Channel 时以事务模式创建。

## 运行验证

```bash
cd mq/rabbitmq/docker && docker compose up -d
cd mq/rabbitmq && mvn spring-boot:run -pl rabbitmq-springboot-transaction

# 提交链路：消费者收到
curl -X POST "localhost:18082/pay?orderNo=D001&userId=4001&points=100"

# 回滚链路：消费者永远收不到
curl -X POST "localhost:18082/pay/fail?orderNo=D002&userId=4002&points=100"
```

预期日志：

```
[Tx] 开启 AMQP 事务，body=PointsMessage[orderNo=D001, ...]
[Tx] txCommit 成功，消息已确认写入 Broker
[Consumer] 收到积分消息，orderNo=D001 ...

[Tx] 开启 AMQP 事务，body=...，随后模拟业务异常
[Tx] 业务异常，txRollback 回滚，消息不会到达消费者      ← D002 无消费日志
```

## 适用场景与局限

- **适合**：对吞吐要求极低、需要「发出去就一定确认」的强一致小流量场景；教学理解事务语义
- **局限**：
  - 事务模式是**同步阻塞**的，吞吐比 Confirm 异步机制低 1~2 个数量级，生产上极少使用（Confirm 取代了它）
  - 和 Confirm 一样，只管「消息到 Broker」，不管业务与消息的一致性
