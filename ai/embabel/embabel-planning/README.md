# embabel-planning — GOAP 多步规划

## 演示内容

Agent 的核心卖点：**不写编排代码**。把任务拆成若干 `@Action`，用强类型领域对象串联，
由 GOAP（Goal Oriented Action Planning）规划器自动推导执行顺序。

流水线：`UserInput -> Research -> Outline -> Article`

## 关键 API

| API | 作用 |
|---|---|
| 多个 `@Action` | 每个动作声明"需要什么类型、产出什么类型" |
| `@AchievesGoal` | 标记最终动作，产出目标类型 |
| `ai.withDefaultLlm().creating(T.class).fromPrompt(...)` | 让 LLM 直接产出强类型对象 |
| `AgentInvocation.create(platform, Article.class)` | 只声明最终目标，中间步骤交给规划器 |

## 接口

```bash
curl -G --data-urlencode "topic=微服务架构的优势" http://localhost:8890/plan/generate
```

`topic` 缺省为「微服务架构」。

## 运行

```bash
cd ai/embabel
mvn -pl embabel-planning spring-boot:run
```

## 代码结构

- `WritingAgent.java` — 三个动作：`research` → `outline` → `write`
- `Research.java` / `Outline.java` / `Article.java` — 领域类型（record），作为动作间的输入输出
- `PlanningController.java` — `GET /plan/generate`，目标类型为 `Article`

## 要点

- 动作之间通过**类型**连接：`write` 需要 `Outline`，规划器就去找能产出 `Outline` 的动作，依次反推。
- 同一套动作可以在不同场景被复用——这是"规划"相对"写死工作流"的最大区别。
- 规划器类型可换（见 [embabel-planner-types](../embabel-planner-types/README.md)）。
