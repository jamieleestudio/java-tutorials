# embabel-budget — 运行预算与熔断

## 演示内容

"看得见成本"（`embabel-observability`）不等于"花不超"。本模块解决**花不超**：

1. **三重预算熔断**：动作数 / token / 成本，任一超限即**提前终止**进程
2. **提前终止事件**：拿到"被哪条策略、以什么原因掐断"，以及被掐断时已消耗多少
3. **限速**：`ProcessControl` 的 `toolDelay` / `operationDelay`，避免打爆下游
4. **成本/价值声明**：`@Action(cost = ..., value = ...)`，作为规划依据与成本基线

## 关键 API

| API | 作用 |
|---|---|
| `new Budget(cost, actions, tokens)` | 三维预算（默认 `$2.0 / 50 动作 / 1,000,000 token`） |
| `budget.earlyTerminationPolicy()` | 展开成 `firstOf(maxActions, maxTokens, hardBudgetLimit)` |
| `new ProcessControl(toolDelay, operationDelay, policy)` | 限速 + 终止策略 |
| `ProcessOptions.DEFAULT.withBudget(b).withProcessControl(control)` | 组装运行选项 |
| `AgentInvocation.builder(platform).options(opts).build(X.class).invoke(input)` | 带选项调用 |
| `EarlyTerminationPolicy.maxActions(n)` / `.maxTokens(n)` / `.hardBudgetLimit(usd)` / `.ON_STUCK` / `.firstOf(...)` | 单独使用策略 |
| `EarlyTermination`（`AgentProcessEvent` 子类） | **熔断事件**：`getPolicy()` / `getReason()` / `getError()` |
| `Delay.NONE / MEDIUM / LONG` | 限速档位 |
| `@Action(cost = 0.02, value = 0.3)` | 动作的声明成本与价值 |
| `ActionQos` / `@Action(actionRetryPolicy = ...)` | **重试/退避/幂等**（Embabel 里 "QoS" 指这个，不是成本） |

## ⚠️ 最容易踩的坑

**`ProcessOptions.withBudget(...)` 只设置 `budget` 字段，不会更新 `processControl` 里的终止策略。**
策略只在 `ProcessOptions` **构造时**从 budget 派生：

```kotlin
data class ProcessOptions(
    val budget: Budget = Budget(),
    val processControl: ProcessControl = ProcessControl(
        earlyTerminationPolicy = budget.earlyTerminationPolicy(),  // 只在构造时派生
    ),
    ...
)
```

所以下面这样写**预算完全不生效**（本模块第一版就是这样，`maxActions=2` 却跑完了 3 个动作）：

```java
// ❌ 无效：策略没被接进 processControl
ProcessOptions.DEFAULT.withBudget(new Budget(2.0, 2, 1_000_000));
```

正确写法：

```java
// ✅ 显式把 budget 的策略接进 ProcessControl
Budget budget = new Budget(2.0, 2, 1_000_000);
ProcessControl control = new ProcessControl(Delay.NONE, Delay.NONE, budget.earlyTerminationPolicy());
ProcessOptions options = ProcessOptions.DEFAULT.withBudget(budget).withProcessControl(control);
```

## 接口与实测

```bash
# 三重预算分别触发
curl -G --data-urlencode "maxActions=2"        http://localhost:8934/budget/run
curl -G --data-urlencode "maxActions=50" --data-urlencode "maxTokens=1000"  http://localhost:8934/budget/run
curl -G --data-urlencode "maxActions=50" --data-urlencode "maxCost=0.0001"  http://localhost:8934/budget/run

# 限速
curl -G --data-urlencode "maxActions=50" --data-urlencode "toolDelay=MEDIUM" http://localhost:8934/budget/run

# 对照实验：紧预算 vs 宽松预算
curl http://localhost:8934/budget/compare
```

实测（同一请求，只改预算）：

| 预算 | completed | 终止策略 | 动作数 | 成本 | token | 耗时 |
|---|---|---|---|---|---|---|
| `maxActions=2` | ❌ | `MaxActionsEarlyTerminationPolicy` | 2 | $0.0044 | 3,964 | 17.9s |
| `maxTokens=1000` | ❌ | `MaxTokensEarlyTerminationPolicy` | 2 | $0.0025 | 2,335 | 10.2s |
| `maxCost=0.0001` | ❌ | `MaxCostEarlyTerminationPolicy` | 1 | $0.0008 | 678 | 4.4s |
| 默认（宽松） | ✅ | — | 3 | $0.0078 | 6,865 | 33.7s |
| 默认 + `toolDelay=MEDIUM` | ✅ | — | 3 | $0.0138 | 11,891 | **57.5s** |

被掐断时的响应（注意 `completed=false`、`terminated=true`、只执行了 2 个动作）：

```json
{"completed":false,
 "executedActions":["com.third.li.PipelineAgent.analyze","com.third.li.PipelineAgent.draft"],
 "terminated":true,
 "terminationReason":"Max actions of 2 reached",
 "terminationPolicy":"MaxActionsEarlyTerminationPolicy",
 "terminationIsError":true,
 "actions":2,"cost":0.0044,"tokens":3964}
```

日志侧也能看到事件：

```
BudgetEventListener - 提前终止：policy=MaxActionsEarlyTerminationPolicy, error=true, reason=Max actions of 2 reached
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-budget spring-boot:run
```

## 代码结构

- `PipelineAgent.java` — 三步流水线（分析→起草→定稿），每步声明 `cost` / `value`
- `BudgetEventListener.java` — 捕获 `EarlyTermination` 事件，并记录最近的 `AgentProcess`
- `BudgetController.java` — `/budget/run`、`/budget/compare`
- `BudgetReport.java` — 报告（预算配置 / 是否完成 / 终止原因 / 实际消耗）

## 要点

- **预算检查发生在动作之间**，不是动作内部。所以：
  - 单动作里的长工具循环**不会**被 `maxActions` 打断（`maxActions` 数的是动作，不是工具调用）；
  - 用 token/成本预算时会有**超调**（实测 `maxTokens=1000` 实际用了 2,335）——
    它是"下一个动作开始前检查"，不是硬性精确截断。
- **动作数上限只在多动作流程里才有意义**。本模块刻意做成 3 个动作就是为了能演示它。
- **终止是否算错误**：`MaxActions` / `MaxTokens` / `MaxCost` 标记 `error=true`，
  而 `ON_STUCK` 是 `error=false`（"卡住"在 utility 规划下是正常结局）。
- **被掐断后 `invoke` 会抛异常**（因为目标类型始终没被产出，本模块实测是
  `NullPointerException: get(...) must not be null`）。生产上应该捕获它，并把它当作
  "预算耗尽"而不是"程序 bug"来处理——这正是 `terminationIsError` 与 `getReason()` 的用途。
- **限速**用 `Delay` 档位（NONE/MEDIUM/LONG）；需要更细的节流要在工具实现里自己做。
- **`ActionQos` 不是成本**：它管重试（`maxAttempts` / `backoffMillis` / `backoffMultiplier` /
  `idempotent`）。成本/价值走 `@Action(cost, value)`，供 GOAP 规划器权衡。
- 与 `embabel-observability` 的分工：那里是**观测**（事件监听、打印成本/token），
  这里是**约束**（超限即终止）。
