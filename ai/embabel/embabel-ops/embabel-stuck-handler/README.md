# embabel-stuck-handler — 进程卡住时的兜底

## 演示内容

规划器**找不到路**时（缺前提、条件永不满足、工具集合不足），进程会进入 `STUCK`。
`StuckHandler` 就是让**你**来补救的钩子：

1. **补救路径**：卡住 → handler 往黑板补上缺失的前提（副作用）→ 返回 `REPLAN` → 框架**重新规划**并继续跑完
2. **对照路径**：同一个"缺前提"的结构，但 Agent 没实现 `StuckHandler` → 进程停在 `STUCK`，目标不达成

## 关键 API

| API | 作用 |
|---|---|
| `@Agent` 类 **`implements StuckHandler`** | **注册方式**：框架读取 Agent 元数据时用 `instance as? StuckHandler` 认出来 |
| `StuckHandler.handleStuck(AgentProcess)` | 卡住时回调；**通过修改 `AgentProcess` 产生副作用**来解除卡住 |
| `new StuckHandlerResult(message, handler, code, process)` | 返回值，本身也是 `AgentProcessEvent`（会被发布，可被监听） |
| `StuckHandlingResultCode.REPLAN` | 已解决 → 框架 `setStatus(RUNNING)` 后**重新 `run()`** |
| `StuckHandlingResultCode.NO_RESOLUTION` | 解决不了 → 框架 `setStatus(STUCK)` |
| `StuckHandler.invoke(vararg)` / `MulticastStuckHandler` | 组合多个 handler（按顺序试，第一个非 `NO_RESOLUTION` 的胜出） |
| `AgentProcess.addObject(...)` / `last(Class)` | `AgentProcess extends Blackboard`，所以能直接读写黑板 |

> **注册方式是最容易找不到文档的一点**：不是 `@Bean StuckHandler`，也不是配置项，
> 而是**让 `@Agent` 注解的那个类自己实现 `StuckHandler`**
> （见 `AgentMetadataReader.kt`：`stuckHandler = instance as? StuckHandler`）。

## 接口与实测

```bash
# 有 handler：卡住 → 补前提 → 重规划 → 完成
curl http://localhost:8935/stuck/recover

# 对照组：无 handler → 停在 STUCK
curl http://localhost:8935/stuck/no-handler
```

实测 `/stuck/recover`（`handlerCalls=1`、`REPLAN`、最终 `COMPLETED`）：

```json
{"goalAchieved":true,
 "result":"经综合评估：项目需求真实…由 StuckHandler 补齐相关分析后，建议按计划推进后续工作。",
 "status":"COMPLETED","stuck":false,
 "handlerCalls":1,"handlerCodes":["REPLAN"],
 "handlerMessages":["已补齐缺失的 Analysis，请求重规划"],
 "executedActions":["com.third.li.RecoveringAgent.write"]}
```

实测 `/stuck/no-handler`（`STUCK`、没有动作执行）：

```json
{"goalAchieved":false,"result":null,"status":"STUCK","stuck":true,
 "handlerCalls":0,"executedActions":[],
 "error":"NullPointerException: get(...) must not be null"}
```

框架日志（`recover` 路径）：

```
Embabel              - [dreamy_dirac] stuck at:
RecoveringAgent      - StuckHandler 已向黑板补齐缺失的 Analysis，请求重规划
StuckEventListener   - StuckHandler 结果：code=REPLAN, message=已补齐缺失的 Analysis，请求重规划
SimpleAgentProcess   - Process dreamy_dirac unstuck and will replan: 已补齐缺失的 Analysis，请求重规划
Embabel              - [dreamy_dirac] (stuck-write) using LLM deepseek-flash, creating String: ...
```

框架日志（`no-handler` 路径）：

```
Embabel - Process xxx is stuck with no StuckHandler. This may or may not be an error. History (0):
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-stuck-handler spring-boot:run
```

## 代码结构

- `RecoveringAgent.java` — **实现 `StuckHandler`**；目标动作需要 `Analysis` 但没人产出它
- `UnhandledAgent.java` — 对照组，同样缺前提但**不**实现 `StuckHandler`
- `Analysis` / `Report` / `UnhandledReport` — 类型（注意两个 Agent 目标类型不同，避免歧义）
- `StuckEventListener.java` — 捕获 `StuckHandlerResult` 事件 + 记录最近的 `AgentProcess`
- `StuckController.java` — `/stuck/recover`、`/stuck/no-handler`
- `StuckOutcome.java` — 观察结果（状态 / 是否卡住 / handler 调用次数与结论码 / 执行过的动作）

## 要点

- **必须自己写终止条件**：handler 里要判断"是否已经补过了"。本模块用
  `process.last(Analysis.class) == null` 作为闸门——补过一次后再卡住就返回 `NO_RESOLUTION`。
  否则 `REPLAN` 会触发 `run()` 递归重规划，可能死循环。
- **补救手段是"改世界状态"**，不是"返回一个答案"：往黑板补对象、`setCondition(...)`、
  或者移除/替换掉某个导致规划失败的对象。返回值只表达"能不能继续"。
- `StuckHandlerResult` 是**事件**，所以可以像本模块一样用 `AgenticEventListener` 观测，
  也可以接监控/告警（"这个 Agent 一天卡了 37 次，每次都是同一个原因"）。
- 与相近机制的分工：
  - **`embabel-replanning`** —— 工具**主动**报告失败后换路（`Tool.replanWhen` / `ReplanRequestedException`）。
  - **`embabel-stuck-handler`（本模块）** —— 规划器**自己**找不到路时的兜底钩子。触发时机不同，可同时使用。
  - **`embabel-budget`** —— "超预算就终止"；StuckHandler 是"卡住就补救"。
- 什么时候不该用：如果"卡住"是正常状态（比如 chatbot 等用户输入），
  不要写 handler——框架在这种情况下只记 debug 日志而不报警
  （源码里区分了 `plannerType.needsGoals`）。
- 对照组里 `invoke` 抛的 `NullPointerException: get(...) must not be null` 是"目标类型始终没被产出"
  的表现。生产上应捕获它，并配合 `process.getStatus() == STUCK` 判定为"规划失败"而非程序 bug。
