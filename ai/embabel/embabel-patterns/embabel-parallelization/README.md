# embabel-parallelization — 并行化（Sectioning / Voting）

## 演示内容

Anthropic《Building Effective Agents》里的 **Parallelization**，两个变体都做了：

- **Sectioning（分片）**：把任务拆成**互不依赖**的子任务并行处理，再汇总。
  典型用法：一次调用干正事、另一次调用做检查（guardrail）；或对同一内容从多维度分别评审。
- **Voting（投票）**：把**同一个任务**跑多次（不同视角/提示词），按多数票提高置信度。

## 关键 API / 配置

| 项 | 作用 |
|---|---|
| 多个独立 `@Action`（都只依赖 `UserInput`） | 并行执行的基础 |
| 汇总动作同时需要多份结果 | 规划器会把独立动作都跑完再汇总 |
| `embabel.agent.platform.process-type: CONCURRENT` | 允许并发执行独立动作 |
| 动作内循环多次调用 LLM | Voting 的实现方式 |

## 接口

```bash
# Sectioning：安全性 / 性能 / 可维护性 三维并行评审 + 汇总
curl -G --data-urlencode "message=用 Redis 缓存热点商品数据并设置 5 分钟过期" http://localhost:8918/parallel/sectioning

# Voting：三个视角独立判定，按多数票出结论
curl -G --data-urlencode "message=给用户发一封包含折扣码的营销邮件" http://localhost:8918/parallel/voting
```

实测日志证明是**真并发**（三个动作同一时刻在不同线程启动）：

```
18:13:54.208 [embabel-platform-1] executing action ...SectioningAgent.security
18:13:54.208 [embabel-platform-2] executing action ...SectioningAgent.performance
18:13:54.208 [embabel-platform-3] executing action ...SectioningAgent.maintainability
```

投票结果示例：`{"decision":"有问题","votes":["有问题","没问题","有问题"],...}`

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-parallelization spring-boot:run
```

## 代码结构

- `SectioningAgent.java` — 三个维度评审 + `report` 汇总
- `VotingAgent.java` — 三个视角独立判定 + 多数票
- `ParallelizationController.java` — 两个端点

## 要点

- 与 `embabel-workflows`（Kotlin）的关系：那里的 `ScatterGather` 就是 Sectioning 的**显式原语**
  （可指定 `maxConcurrency`），`Consensus` 则是 Voting 的显式原语。本模块用"普通动作 + 汇总动作"
  的方式在 Java 里实现同样效果，便于对照理解。
- 与 `embabel-orchestrator-workers` 的区别：sectioning 的子任务**预定义且固定**；
  orchestrator 的子任务**运行期才由模型确定**。
- 并发会放大 token 成本与限流风险，注意 `maxConcurrency` 与重试策略。
