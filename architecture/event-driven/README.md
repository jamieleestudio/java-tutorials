# ④ 事件驱动架构 (Event-Driven Architecture — 务实版)

## 概述

与 ③ 的同步 RPC 形成**分布式两种范式对比**：Kafka 异步事件 + **本地消息表（Outbox）** + **Saga 事件协同** + **最终一致性**。不做 CQRS/事件溯源（生产采用率低）。

## 与 ③ 的本质差异

| | ③ 同步 RPC | ④ 事件驱动 |
|---|---|---|
| 通信 | 同步 HTTP | **异步 Kafka** |
| 一致性 | 即时 | **最终一致**（查状态接口兜底） |
| 耦合 | 运行时耦合（等响应） | **时间解耦**（发布者不等消费者） |
| 故障 | 调用链中断 | 消费者宕机不影响下单 |
| 消息可靠性 | — | **Outbox**（业务+消息同事务） |
| 重复消费 | orderId 幂等 | **eventId 去重表** |

## Saga 流程（事件协同，无中心协调器）

```
POST /api/orders → order本地事务: [保存订单 + Outbox写OrderCreatedEvent] → 立即返回

PaymentService 消费 OrderCreatedEvent → 支付 → Outbox写PaymentSucceededEvent
ProductService 消费 OrderCreatedEvent → 扣库存(预校验) → Outbox写InventoryDeductedEvent

OrderService 消费两者:
  PaymentSucceeded  → order.markPaymentSucceeded()
  InventoryDeducted → order.markInventoryDeducted()
  两者都到 → 状态 CONFIRMED ✅

失败补偿:
  支付失败/库存不足 → order.cancel → OrderCancelledEvent
  → PaymentService 退款、ProductService 补货（凭 deducted_orders 记录精确补偿）
```

## 模块结构（5 个）

```
event-driven/
├── shared-kernel/     纯内核 + 集成事件契约(shared.event: OrderCreated/PaymentSucceeded/... + EventEnvelope)
├── product-api/       同步目录读契约（务实混合：同步读 + 异步写）
├── order-service/     单模块4层: Saga状态机domain + Outbox + Kafka消费(payment-events/product-events)
├── payment-service/   消费 order-events → 支付/退款 + Outbox
└── product-service/   消费 order-events → 扣库存/补货 + deducted_orders 补偿记录 + /rpc/products 读
```

## 关键机制

**本地消息表（Outbox）**：`OutboxEventPublisher`（实现应用层 `EventPublisherService`）把事件写入 `outbox_messages` 表——与业务数据**同一本地事务**；`OutboxRelay`（@Scheduled 1s）轮询发布到 Kafka。不丢消息。

**消费幂等**：`processed_events` 表按 `eventId` 去重（at-least-once 投递下重复消费安全）。

**Order Saga 状态机**：`CREATED → (paymentSucceeded && inventoryDeducted) → CONFIRMED`，任一失败 → `CANCELLED` + 补偿事件。

## 运行（需 Kafka）

```bash
cd architecture/event-driven
mvn spring-boot:run -pl product-service &   # 8083
mvn spring-boot:run -pl payment-service &   # 8082
mvn spring-boot:run -pl order-service       # 8081
# KAFKA_SERVERS 环境变量覆盖默认 localhost:9092
```

## 测试

```bash
mvn test   # 14 个：7 Saga领域状态机 + 2 Outbox中继(Mockito) + 5 ArchUnit
```
Kafka 不可用也不影响测试（消费者/KafkaTemplate 均 mock 或不加载）。