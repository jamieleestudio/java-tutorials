# embabel-supervisor — 主管模式（Supervisor）

## 演示内容

一个 **LLM 主管**按需调度多个动作完成目标（类似 LangGraph 的 supervisor 模式）：
框架把"非目标动作"暴露成工具，主管 LLM 在循环里决定"下一步调哪个"，输入齐备后自动执行目标动作。

## 关键 API

| API | 作用 |
|---|---|
| `@Agent(planner = PlannerType.SUPERVISOR)` | 启用主管模式 |
| `@AchievesGoal` | **有且仅有一个**，作为最终目标动作 |
| 其余 `@Action` | 自动被包装成"curried 工具"（已在黑板上的输入会被柯里化掉） |

## 接口

```bash
curl -G --data-urlencode "message=为什么 Agent 需要类型化的领域模型？" http://localhost:8912/supervisor/ask
```

日志里能看到主管的决策循环：

```
Supervisor iteration 1: 2 curried tools (from 2 actions)
Supervisor iteration 1 response: I have enough information to produce the final answer.
...
goal com.third.li.SupervisorAgent.write achieved
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-supervisor spring-boot:run
```

## 代码结构

- `SupervisorAgent.java` — `gatherFacts` / `makeOutline`（工人动作）+ `write`（唯一目标动作）
- `SupervisorController.java` — `GET /supervisor/ask`

## 要点

- **约束**：SUPERVISOR 要求该 Agent **有且仅有一个** `@AchievesGoal` 动作，否则启动校验失败
  （`AgentMetadataReader` 会报错）。
- 主管循环**最多 10 轮**（框架内置的安全上限），因此动作别设计得太碎。
- 与 `embabel-planner-types` 的区别：那里是"规划器类型"（GOAP/UTILITY 对比），这里是
  **"由 LLM 当主管动态编排"** 的编排模式。
- 与 `embabel-subagent` 的区别：subagent 是"把另一个 Agent 当工具"，supervisor 是"把**本 Agent 的动作**当工具交给 LLM 编排"。
