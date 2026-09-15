# embabel-multi-goal — 多目标自动选择

## 演示内容

同一个 Agent 声明**多个目标**，调用方**不指定**要哪个，由平台的**排序器**选择——
这是"智能体自主选择目标"的基础能力。

## 关键 API

| API | 作用 |
|---|---|
| 多个 `@AchievesGoal` | 一个 Agent 多个目标（产出不同类型） |
| `embabel.agent.platform.ranking.llm` | 指定用于**目标/Agent 排序**的模型（不配则用默认排序） |
| `Ranker` / `LlmRanker` | 排序实现；`AgentProcess.getGoal()` 可看到最终选中的目标 |
| `AgentInvocation.on(platform)` | `resultType = Any`：任何目标都匹配，把选择权交给排序器 |

## 接口

```bash
curl -G --data-urlencode "message=引入 Agent 框架需要先做哪些准备？" http://localhost:8915/multi-goal/auto
```

返回示例（节选）：

```json
{"goal":"com.third.li.MultiGoalAgent.summarize","status":"COMPLETED","result":{"content":"..."}}
```

日志里能看到排序模型与最终选中的目标：

```
LlmRanker - Using LLM 'deepseek-flash' for ranking
goal com.third.li.MultiGoalAgent.summarize achieved
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-multi-goal spring-boot:run
```

## 代码结构

- `MultiGoalAgent.java` — `summarize` / `critique` / `toSteps` 三个目标
- `MultiGoalController.java` — `GET /multi-goal/auto`
- `GoalSelection.java` — 返回选中的目标与产出

## 要点（重要语义）

- **`AgentInvocation.create(platform, X.class)` 里的 `X` 用来选 _Agent_，不用于在同一 Agent 的多个目标间路由**：
  框架找的是"第一个有目标产出类型可赋给 X 的 Agent"（`findAgentByResultType`），
  然后 Agent 内部的目标由**排序器**决定。
  因此如果只声明类型而 Agent 有多个目标，可能得到别的目标产出，甚至取结果时为空。
- 要让调用方**精确指定**产出类型，可靠做法是**一个 Agent 一个目标**（本仓库大多数模块都是这样）。
- 多目标的价值在于**自主性**：同一个输入可以产出摘要/评审/步骤，由平台按排序（可换 LLM 排序器）
  与置信度阈值决定走哪条路；也可配合 `GoalApprover` 做人工/规则审批（见框架 API）。
