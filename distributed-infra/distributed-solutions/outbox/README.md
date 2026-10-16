# distributed-solutions-outbox：Outbox 模式（本地消息表）+ 定时任务消费

`distributed-infra` 通用分布式解决方案系列的第一个模块。

## 概述

**解决的问题**：业务操作与其"后续动作"（执行本地业务 / 发 MQ）无法天然放进同一个事务——

- 先落业务、再发消息 → 发送失败则**丢事件**
- 先发消息、再落业务 → 消息先到而业务未落库

**核心思想**：

```
业务事务（同一个本地事务，原子提交）:
    业务表写入（订单） + outbox 事件写入（PENDING）
              ↓
定时任务（OutboxRelay，@Scheduled 轮询）:
    认领到期事件（FOR UPDATE SKIP LOCKED）
        → 逐条消费（OutboxPublisher.publish）
              → 成功: status=PUBLISHED
              → 失败: retry_count+1, 指数退避 next_retry_at
                        → 超过 max_retries: status=FAILED（死信）
```

**术语说明**：中文语境的"本地消息表"与英文文献的 "Outbox" 是同一个模式的两种叫法。
捞取到事件后**直接执行本地业务**（本模块 Demo 的做法）与**投递到 MQ** 交给下游，
都是 Outbox 消费侧的实现变体，模式骨架完全相同。

## 关键机制

| 机制 | 实现 |
|---|---|
| 原子性 | 业务数据与 outbox 记录同一本地事务写入，业务回滚则事件不存在（不多不丢） |
| 并发安全认领 | `SELECT ... WHERE status='PENDING' AND next_retry_at<=now LIMIT n FOR UPDATE SKIP LOCKED`（MySQL 8.0+），多实例并发扫描互不阻塞、不重复认领 |
| 状态流转 | PENDING → PUBLISHED / FAILED；失败重试不占坑（退避期间不可被捞取） |
| 重试退避 | `next_retry_at = now + initial_backoff × multiplier^(retry_count-1)`，默认 5s 起、2 倍退避、5 次封顶 |
| 至少一次 | 认领后进程宕机，记录仍是 PENDING，会被再次捞取 → **消费方必须幂等** |
| 表结构 | `(status, next_retry_at)` 组合索引精确匹配扫描条件；`ddl-auto=validate` + 启动时幂等 DDL（`CREATE TABLE IF NOT EXISTS`） |

**SKIP LOCKED vs 乐观抢占**：另一种常见认领写法是 `UPDATE ... SET ... WHERE status='PENDING' LIMIT n`
靠影响行数抢占，少一次查询但拿不到整行数据、且与 MySQL 的 LIMIT 语义有坑；SKIP LOCKED 语义清晰、
行锁不阻塞他人，是 MySQL 8.0+ 的推荐做法（本文采用）。

**局限（生产化方向）**：认领与消费在同一个事务中，行锁持有时间受消费动作耗时影响，
`batch-size` 不宜过大；更长久的方案是"认领 + PROCESSING 状态 + 超时回收"，或改用 CDC
（如 Debezium 监听 binlog）代替定时任务扫描。

## 模块结构

```
outbox/
├── pom.xml                                  distributed-solutions-outbox
├── docker-compose.yml                       MySQL 8 环境（首次启动自动建库建表）
└── src/
    ├── main/java/com/yineng/bpe/distributedsolutions/outbox/
    │   ├── OutboxStatus.java                PENDING / PUBLISHED / FAILED
    │   ├── OutboxMessage.java               本地消息表实体
    │   ├── OutboxMessageRepository.java     认领查询（SKIP LOCKED）
    │   ├── OutboxPublisher.java             消费 SPI（执行本地业务 or 投递 MQ）
    │   ├── LoggingOutboxPublisher.java      默认实现（日志），业务 Bean 自动替换
    │   ├── OutboxRelay.java                 定时任务核心：认领 → 消费 → 状态流转
    │   ├── OutboxRelayProperties.java       outbox.relay.* 配置项
    │   ├── OutboxAutoConfiguration.java     自动装配（含条件化 @EnableScheduling）
    │   ├── DemoOrder.java                   演示用业务表
    │   ├── DemoOrderRepository.java
    │   ├── OrderEventHandler.java           演示：本地业务处理器（前两次失败演示重试）
    │   ├── OutboxDemoApplication.java       Demo 启动类
    │   └── OutboxDemoRunner.java            三个演示场景
    ├── main/resources/
    │   ├── application.yml                  数据源 / relay 配置
    │   ├── db/mysql/schema.sql              MySQL 8 DDL（幂等）
    │   └── META-INF/spring/...AutoConfiguration.imports
    └── test/java/.../outbox/it/             集成测试（连真实 MySQL）
        ├── OutboxTestApplication.java
        ├── RecordingPublisher.java          记录型消费实现
        └── OutboxRelayTest.java             5 个用例
```

## 环境准备与运行

```bash
# 1. 启动 MySQL 8（首次启动自动建库建表；宿主机端口 33061 -> 容器 3306，
#    本机 3306 通常已被本地 MySQL 占用；如 33061 也被占用，改 docker-compose.yml
#    端口映射并以 MYSQL_PORT 环境变量同步配置）
cd distributed-infra/distributed-solutions/outbox
docker compose up -d
docker compose ps   # 等待状态变为 healthy

# 2. 跑测试（MySQL 不可达时测试自动 skip，不会报错挂掉）
mvn -pl distributed-infra/distributed-solutions/outbox -am test

# 3. 跑 Demo，观察三个场景的控制台输出
mvn -pl distributed-infra/distributed-solutions/outbox -am package -DskipTests
java -jar distributed-infra/distributed-solutions/outbox/target/distributed-solutions-outbox-1.0-exec.jar
```

Demo 覆盖三个场景：

1. **场景一**：订单与事件同事务提交 → 定时任务捞取 → 本地处理器执行 → PENDING→PUBLISHED
2. **场景二**：业务中途抛异常 → 同事务回滚 → outbox 无事件泄漏（对比回滚前后总行数）
3. **场景三**：处理器前两次失败 → 观察指数退避重试日志 → 第三次成功

## 配置项（`outbox.relay.*`）

| 配置 | 默认值 | 说明 |
|---|---|---|
| `enabled` | `true` | 关闭后不启动定时调度，`OutboxRelay` Bean 仍可注入手动触发 |
| `poll-interval` | `1000` | 轮询间隔（毫秒；`@Scheduled` 仅支持纯数字或 ISO-8601，不支持 `1s` 简写） |
| `batch-size` | `50` | 每批最多认领条数 |
| `max-retries` | `5` | 最大重试次数，达到后转 FAILED 死信 |
| `initial-backoff` | `5s` | 首次失败退避时间 |
| `backoff-multiplier` | `2.0` | 退避倍数 |

## 如何接入业务

1. 引入 `distributed-solutions-outbox` 依赖
2. 实现 `OutboxPublisher` 并注册为 Spring Bean（自动替换默认日志实现）——捞取到事件后执行你的业务
3. 业务代码里，与业务数据**同一个事务**内调用 `outboxRepository.save(OutboxMessage.pending(...))`
4. 若应用主包不包含 `com.yineng.bpe.distributedsolutions.outbox`，
   启动类追加 `@EntityScan` / `@EnableJpaRepositories` 指向该包

## 测试用例

| 用例 | 验证点 |
|---|---|
| relayOnce_publishes_due_message_and_marks_published | 到期消息被消费并标记 PUBLISHED |
| relayOnce_skips_messages_not_due_yet | 未到期（next_retry_at 在未来）的消息不被捞取 |
| relayOnce_retries_with_backoff_then_marks_failed_after_max_retries | 退避期间不可捞取、重试计数、超限转 FAILED |
| concurrent_relays_claim_disjoint_batches_without_duplicates | 4 并发 relay 认领 20 条，恰好各消费一次（SKIP LOCKED 生效） |
| rollback_leaves_no_outbox_row_and_no_business_row | 同事务回滚后无事件、无业务数据泄漏 |

## Roadmap

- [ ] 压测：MySQL 压测入口（写 TPS / 消费吞吐 / p95-p99 延迟 / 重复处理校验）
- [ ] KafkaOutboxPublisher：投递到 MQ 的消费实现（新子模块）
- [ ] 幂等消费表（processed_events）：at-least-once 语义下的消费方幂等方案
- [ ] CDC（Debezium）方案对比：binlog 代替定时任务扫描
- [ ] FAILED 死信的重新入队管理接口
