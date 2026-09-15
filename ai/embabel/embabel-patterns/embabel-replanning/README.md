# embabel-replanning — 动态重规划

## 演示内容

工具执行结果不达标时**触发重规划**，让规划器换条路走（而不是把失败处理写死在业务代码里）。

流程（实测日志）：

```
failed tool lookup_topic -> ReplanRequestedException: lookup_topic replans based on result
Tool 'lookup_topic' requested replan
tool loop completed ... replan=true
Action ... requested replan: ... Blacklisted for next cycle.
executing action com.third.li.ReplanningAgent.answer      ← 重规划后再次执行
tool loop completed ... replan=false                      ← 第二次工具调用成功
completed
```

## 关键 API

| API | 作用 |
|---|---|
| `Tool.replanWhen(tool, predicate)` | 工具 artifact 命中谓词 → 要求重规划 |
| `Tool.replanAlways(tool)` | 工具执行后总是重规划 |
| `Tool.replanAndAdd(tool, valueComputer)` | 重规划并把计算结果放进黑板 |
| `Tool.conditionalReplan(tool, decider)` | 用 `ReplanContext` 做更精细的决策 |
| `@Action(canRerun = true)` | **必需**：允许动作在重规划后再次执行 |
| `ReplanRequestedException` | 也可在动作里主动抛出以要求重规划 |

## 接口

```bash
curl -G --data-urlencode "message=帮我查一下 Embabel 的资料要点" http://localhost:8914/replan/ask
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-replanning spring-boot:run
```

## 代码结构

- `ReplanningAgent.java` — 一个"第一次必失败"的工具（`Tool.Result.withArtifact(content, "FAILED")`），
  用 `Tool.replanWhen(..., "FAILED")` 包装；动作 `canRerun = true`
- `ReplanningController.java` — `GET /replan/ask`

## 要点

- **artifact 是关键**：`replanWhen` 的谓词接收的是工具的 **artifact**，所以工具要用
  `Tool.Result.withArtifact(content, artifact)` 返回结构化结果；只返回文本的话谓词拿不到东西。
- 重规划后框架会把该动作**在本轮黑名单**（"Blacklisted for next cycle"），因此：
  - 单动作 Agent：会在下一个周期重跑该动作（本示例就是靠重跑 + 工具第二次成功）；
  - 多动作 Agent：可以借此**切换到备用动作**（更接近"换条路走"的语义）。
- 想主动要求重规划，直接在动作里抛 `ReplanRequestedException`；工具循环里还有 `ReplanningTools` 可用。
- 与"重试"的区别：`@Action(actionRetryPolicy=...)` 是**同一动作重试**（幂等失败恢复）；
  重规划是**重新规划路径**（换动作/换顺序）。
