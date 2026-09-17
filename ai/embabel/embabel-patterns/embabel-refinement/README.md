# embabel-refinement — 自评迭代（Evaluator-Optimizer）

## 演示内容

让模型给自己的产出打分，不达标就带着评审意见重做，最多迭代 N 轮。

流程：`生成 -> 评分(suggestion) -> 未达标则改进重做 -> ... -> 达标或到上限即停`

## 关键 API

| API | 作用 |
|---|---|
| `Feedback`（框架接口） | `getScore()` 返回 0~1，评审结果实现它 |
| `creating(Evaluation.class)` | 让模型产出结构化的评分 + 建议 |
| 手写循环 | 控制迭代次数与停止阈值（本示例 3 轮 / 阈值 0.85） |

> 框架在 Kotlin DSL 里提供同款原语 `RepeatUntilAcceptable(maxIterations, scoreThreshold)`，
> 内部会生成"任务动作 + 评估动作 + 接受条件 + 取历史最优"的完整规划。
> 本模块用 Java 手写同一模式，便于看清每轮控制流。

## 接口

```bash
curl -G --data-urlencode "message=写一句面向新用户的 Embabel 简介" http://localhost:8899/refine
```

返回：

```json
{"text":"...","score":0.85,"attempts":2,"lastSuggestion":"..."}
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-refinement spring-boot:run
```

## 代码结构

- `Evaluation.java` — record 实现 `Feedback`，含 `score` 与 `suggestion`
- `RefinementAgent.java` — 生成/改进 + 评分，达标提前退出
- `RefinedText.java` — 最终文本 / 分数 / 实际轮数 / 最后建议

## 要点

- 这是 Anthropic "Building Effective Agents" 里的 Evaluator-Optimizer 模式。
- 每轮会多一次（或多次）LLM 调用，注意成本：可用更便宜的模型做评审，或设置更小的轮数上限。
- 评分本身也是 LLM 输出，可能有波动；生产环境常配合"取历史最优"（框架原语内置 `bestSoFar()`）。


## 附：框架自带的循环原语（未单独建模块）

本模块手写了 evaluator-optimizer 循环（critique → revise → 再 critique）。
框架还提供了两个**现成的循环 builder**：

| Builder | 作用 |
|---|---|
| `RepeatUntilBuilder` | 重复某段流程直到谓词满足（`Looper` + `Emitter`） |
| `RepeatUntilAcceptableBuilder` | 重复直到"评审者认为可接受"（内置 `Critiquer` / `Evaluator`） |

`RepeatUntilAcceptableBuilder` 与本模块做的事**基本等价**，区别是：
- 用 builder：少写代码，但循环控制（迭代上限、接受标准）要按它的约定来；
- 手写（本模块）：控制更细（比如"评分连续两轮不提升就停"），也更容易加预算约束（见 `embabel-budget`）。

**选择建议**：先用 builder，发现它的约定不够用再手写。
