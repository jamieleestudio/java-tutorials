# rabbitmq-springboot-tx-message — 方案4：仿 RocketMQ 事务消息（半消息）

## 做什么

在 RabbitMQ 上模拟 RocketMQ **事务消息的两阶段提交 + 消息回查**：先落「半消息」（不可投递），执行本地事务，成功则提交半消息并投递，失败则回滚；进程在中间任何时刻崩溃，由回查任务根据「业务是否真正发生」裁决提交或回滚。消费端幂等防重复发奖。

## 核心机制（状态机）

```
Phase 1  t_tx_message.status = 0 半消息落库          （等价于发送 half message）
Phase 2  本地事务：写订单 t_payment_order             （业务成败在此）
           成功 → status = 1 已提交 → 投递 MQ
           失败 → status = 2 已回滚，消息永不投递
Phase 3  ConfirmCallback ack → status = 3 已送达

回查任务（每 5s，等价于 RocketMQ 消息回查）：
  status=0 且超过 30s → 查订单存在？
      存在   → 提交并补偿投递（等价回查返回 COMMIT）
      不存在 → 回滚（等价回查返回 ROLLBACK）
兜底：status=1 超过 60s 未收到 Confirm → 重发
```

与方案3（本地消息表）的区别：方案3 是「业务先行、消息后补」；本方案是「消息先行占位（半消息）、业务后行、回查裁决」，与 RocketMQ 事务消息语义对齐。

## 数据表（db_txmsg）

| 表 | 作用 |
|---|---|
| `t_tx_message` | 事务消息表，状态机 `0半消息 → 1已提交 → 3已送达 / 2已回滚` |
| `t_payment_order` | 业务订单（回查的裁决依据） |
| `t_points_record` | 消费端落账，`uk_order_no` 幂等 |

## 关键代码

| 文件 | 内容 |
|---|---|
| `service/PaymentService.java` | 三阶段实现 + `TransactionTemplate` 显式事务 + 发送 |
| `task/HalfMessageCheckTask.java` | 回查（半消息裁决）+ 兜底重发 |
| `config/RabbitTxMessageConfig.java` | Confirm 回调标记「已送达」 |
| `consumer/PointsConsumer.java` | 幂等消费 |

## 运行验证

```bash
cd mq/rabbitmq/docker && docker compose up -d
cd mq/rabbitmq && mvn spring-boot:run -pl rabbitmq-springboot-tx-message

# 正常：半消息 → 事务提交 → 投递 → 送达
curl -X POST "localhost:18084/pay?orderNo=T001&userId=2001&points=100"

# 失败：半消息 → 事务回滚 → 消息作废
curl -X POST "localhost:18084/pay/fail?orderNo=T002&userId=2002&points=100"

# 模拟"执行业务前进程崩溃"：只落半消息，不执行业务
curl -X POST "localhost:18084/pay/half?orderNo=T003&userId=2003&points=100"
# 30 秒后回查任务裁决：订单不存在 → status=2 回滚

# 回查发现事务"其实已提交"的场景：造半消息后手工补插订单
curl -X POST "localhost:18084/pay/half?orderNo=T004&userId=2004&points=100"
docker exec rabbitmq-demo-mysql mysql -uroot -proot -e \
  "INSERT INTO db_txmsg.t_payment_order (order_no,user_id,amount) VALUES ('T004',2004,299.00);"
# 30 秒后回查：订单存在 → 提交并投递 → 消费者收到
```

预期状态（查 `t_tx_message`）：

| 单号 | status | 含义 |
|---|---|---|
| T001 | 3 | 已送达，消费者已扣积分 |
| T002 | 2 | 业务回滚，消息作废 |
| T003 | 2 | 回查裁决：订单不存在 → 回滚 |
| T004 | 3 | 回查裁决：订单存在 → 补偿投递成功 |

## 适用场景与局限

- **适合**：需要与 RocketMQ 事务消息对齐语义的架构迁移/对比学习；跨服务最终一致性
- **局限**：半消息、回查、兜底全是自建（RabbitMQ 无原生事务消息）；同样要求**下游幂等**；回查有延迟
