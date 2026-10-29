# rabbitmq-springboot-reliable — 方案3：消息落库 + 事务后发送 + 定时补偿

## 做什么

解决**业务操作与消息发送的最终一致性**：业务数据（支付订单）和消息记录在同一个本地事务里落库，事务提交后再发送消息，Confirm 确认后更新状态，发送失败由定时任务补偿重发，消费端幂等去重。

## 核心机制

```
本地事务 {
    1. 写业务数据 t_payment_order
    2. 写消息记录 t_local_message（status=0 待发送）
} 提交
    ↓ afterCommit（TransactionSynchronization）
3. 发送 RabbitMQ（携带 CorrelationData=messageId）
    ↓ ConfirmCallback（异步）
4. ack=true  → t_local_message.status=1（已发送）
   ack=false → 保持 0，等待补偿
    ↓
定时任务（每 5s）：扫 status=0 且到期且未超上限 → 重发 + 退避递增 retry_count
```

为什么**发送必须在事务提交后**：事务内发送会导致「事务回滚但消息已发出」或「消费者先于事务提交消费到旧数据」；提交后发送的失败由消息表状态兜底，可补偿。

## 数据表（db_reliable）

| 表 | 作用 |
|---|---|
| `t_payment_order` | 业务订单，`uk_order_no` 业务幂等 |
| `t_local_message` | 消息表：状态机 `0待发送 → 1已发送`，retry_count + next_retry_time 驱动补偿 |
| `t_points_record` | 消费端落账表，`uk_order_no` 实现消费幂等 |

## 关键代码

| 文件 | 内容 |
|---|---|
| `service/PaymentService.java` | `@Transactional` 内双写 + `afterCommit` 发送 |
| `config/RabbitReliableConfig.java` | Confirm 回调更新消息状态（ack 机制所在），Return 回调告警 |
| `task/MessageCompensationTask.java` | 定时补偿：扫表重发 + 退避 + 重试上限 |
| `consumer/PointsConsumer.java` | `INSERT IGNORE` 幂等消费，重复消息打 WARN |

## 运行验证

```bash
cd mq/rabbitmq/docker && docker compose up -d
cd mq/rabbitmq && mvn spring-boot:run -pl rabbitmq-springboot-reliable

# 正常链路
curl -X POST "localhost:18083/pay?orderNo=R001&userId=1001&points=100"
# → 订单落库、消息 status=1、积分到账

# 模拟"发送失败"：把消息手工改回待发送，观察补偿重发
docker exec rabbitmq-demo-mysql mysql -uroot -proot -e \
  "UPDATE db_reliable.t_local_message SET status=0, next_retry_time=NOW(), retry_count=0 WHERE biz_key='R001';"
```

预期日志：

```
[Pay] 本地事务提交：订单落库 + 消息落库（待发送），orderNo=R001, messageId=...
[Pay] 事务已提交，开始发送消息，messageId=...
[Confirm] 消息已到达 Broker，标记为已发送，messageId=...
[Consumer] 积分发放成功，orderNo=R001 ...

[Compensate] 扫描到待补偿消息，重发，messageId=..., retryCount=0
[Consumer] 重复消息，幂等拦截，orderNo=R001            ← 补偿重发被幂等吃掉
[Confirm] 消息已到达 Broker，标记为已发送，messageId=...
```

## 适用场景与局限

- **适合**：订单、扣款等强一致性业务，是「发送端一致性」最常用的生产方案
- **局限**：每次发消息多一次落库（性能有代价）；可能重复投递，**下游必须幂等**；补偿任务有延迟（秒级）
