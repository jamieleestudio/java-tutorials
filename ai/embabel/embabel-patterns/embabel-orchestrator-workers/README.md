# embabel-orchestrator-workers — 编排者-工人（动态拆解）

## 演示内容

Anthropic《Building Effective Agents》里的 **Orchestrator-workers**：由一个**中央 LLM 动态拆解任务**
（子任务数量事先不可知），派给 worker 执行，再综合结果。

三步：`decompose`（动态拆解）→ `work`（逐个执行）→ `synthesize`（综合）

## 关键 API

| API | 作用 |
|---|---|
| `ai.creating(Subtasks.class).fromPrompt(...)` | 编排者动态产出**子任务列表**（数量由模型决定） |
| 动作内循环调用 LLM | worker 逐个执行子任务（也可换成 `ScatterGather` 并行） |
| 汇总动作 `@AchievesGoal` | 综合所有 worker 结论 |

## 接口

```bash
curl -G --data-urlencode "message=我们该不该把单体拆成微服务？" http://localhost:8919/orchestrator/ask
```

返回 `{"content":"...","subtaskCount":N}`——`subtaskCount` 就是本次动态拆解出的子任务数。

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-orchestrator-workers spring-boot:run
```

## 代码结构

- `OrchestratorAgent.java` — `decompose` / `work` / `synthesize`
- `Subtasks`、`WorkerResult(s)`、`FinalReport` — 数据载体
- `OrchestratorController.java` — `GET /orchestrator/ask`

## 要点（与相近模式的区别）

- vs **`embabel-supervisor`**：supervisor 是"LLM 逐轮决定调哪个**已有动作**"（动作集合固定）；
  orchestrator 是"先把任务**拆成新子任务**"（任务集合动态生成）。
- vs **`embabel-parallelization`**：sectioning 的子任务**预定义**且固定；orchestrator 的子任务
  **运行期才确定**——这正是文章强调的关键差异，适合"改哪些文件、改几处都取决于任务本身"这类场景。
- 生产上常把 `work` 换成并行（`ScatterGather`/`CONCURRENT`）并给每个 worker 配独立工具集。
