# embabel-programmatic-dsl — 编程式 DSL（Kotlin）

## 演示内容

**用代码构建 Agent，而不是用注解。** 等价于 `@Agent` / `@Action` / `@AchievesGoal`，
但完全类型安全、可在运行时动态拼装。两个例子：

### 1. 显式流水线 `/dsl/pipeline`

```kotlin
agent(name = "DslArticlePipeline", description = "...") {
    condition { topicNonEmpty }                       // 程序化条件（可用作 pre/post）

    promptedTransformer<ArticleRequest, Outline>(     // LLM：输入 -> 结构化输出
        name = "makeOutline",
        pre = listOf(topicNonEmpty),
    ) { ctx -> "为主题「${ctx.input.topic}」生成 3 点提纲..." }

    promptedTransformer<Outline, Draft>(              // LLM
        name = "writeDraft",
    ) { ctx -> "根据下面的提纲写一篇 200 字以内的短文：\n${ctx.input.points.joinToString("\n")}" }

    transformation<Draft, Article>(                   // 纯代码，不调 LLM
        name = "polish",
    ) { ctx -> Article("【DSL】${ctx.input.title}", ctx.input.body, ctx.input.body.length) }

    goal(name = "ProduceArticle", satisfiedBy = Article::class)
}
```

动作顺序由**类型**推导（`ArticleRequest -> Outline -> Draft -> Article`），
不需要手写 pre/post 条件。

实测输出（注意 `【DSL】` 前缀与 `wordCount`，证明纯代码那步确实执行了）：

```json
{"title":"【DSL】类型化建模：让 Agent 可靠、可测、可演进",
 "body":"Agent 的输入输出、工具调用与内部状态本质异构，天然易错...",
 "wordCount":173}
```

### 2. 扇出 + 汇总 `/dsl/digest`

```kotlin
agent(name = "DslAngleDigest", description = "...") {
    flow {
        aggregate<ArticleRequest, AngleNote, Digest>(
            transforms = listOf(
                { ctx -> note(ctx, "技术可行性") },
                { ctx -> note(ctx, "产品价值") },
                { ctx -> note(ctx, "风险与成本") },
            ),
            merge = { notes, ctx -> Digest(topic, summary, notes) },
        )
    }
    goal(name = "ProduceDigest", satisfiedBy = Digest::class)
}
```

`aggregate` 会自动生成"N 个并行转换动作 + 一个汇总动作 + 完成条件"的完整规划。

## 关键 API（`com.embabel.agent.api.dsl`）

| API | 作用 |
|---|---|
| `agent(name, provider, version, description) { ... }` | 构建 `Agent` |
| `AgentBuilder.promptedTransformer<I, O>(...) { ctx -> prompt }` | LLM 动作（结构化输出 `O`） |
| `AgentBuilder.transformation<I, O>(...) { ctx -> O }` | 纯代码动作 |
| `AgentBuilder.goal(name, satisfiedBy = X::class)` | 声明目标类型 |
| `AgentBuilder.conditionOf(name) { ctx -> Boolean }` / `condition { }` | 程序化条件 |
| `AgentBuilder.flow { ... }` | 加入 `TypedAgentScopeBuilder` 模式（多动作时用） |
| `chain / andThen / andThenDo / split / branch / aggregate / repeat` | 类型安全编排原语 |
| `AgentPlatform.deploy(agent)` | 注册到平台 |

## 接口

```bash
curl -G --data-urlencode "topic=为什么 Agent 需要类型化建模" http://localhost:8932/dsl/pipeline
curl -G --data-urlencode "topic=在团队里引入 Agent 框架"     http://localhost:8932/dsl/digest
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-programmatic-dsl spring-boot:run
```

## 代码结构

- `DslAgentConfig.kt` — 两个 DSL agent + `deployDslAgents`（`ApplicationRunner` 显式 `deploy`）
- `Domain.kt` — `ArticleRequest` / `Outline` / `Draft` / `Article` / `AngleNote` / `Digest`
- `DslController.kt` — `GET /dsl/pipeline`、`GET /dsl/digest`

## 要点

- **为什么需要它**：注解式写法适合静态结构；一旦 Agent 需要**按配置/租户/运行时数据拼装**，
  注解就无能为力。DSL 把"构建 Agent"变成普通代码。
- **Kotlin 注意**：Embabel 1.0.0 的 Kotlin metadata 是 2.1.0，需显式覆盖
  `kotlin.version`（Spring Boot 默认管到 1.9.25）；结构化输出的目标是 Kotlin `data class`，
  需加 `jackson-module-kotlin` 依赖。
- **显式注册**：`@Bean Agent` 的自动注册依赖扫描开关，这里用 `ApplicationRunner`
  显式 `agentPlatform.deploy(agent)`，行为确定（与 `embabel-workflows` 一致）。
- vs **`embabel-workflows`**：那里用 `ScatterGatherBuilder` / `ConsensusBuilder` 这类
  **现成编排器**；这里用 `agent {}` + `flow {}` + `aggregate` 这类**底层原语**，自由度更高。
