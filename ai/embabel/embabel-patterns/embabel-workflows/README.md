# embabel-workflows — 工作流原语（Kotlin）

## 演示内容

用 Embabel 内置的**工作流原语**构建 Agent（本模块用 Kotlin 编写，DSL 更自然）：

- **ScatterGather**：同一任务按多个角度**并行**扇出，再**汇总**成一个结论
- **Consensus**：多个来源（这里是两个模型角色）各自回答，再**达成共识**

## 关键 API

| API | 作用 |
|---|---|
| `ScatterGatherBuilder.returning(R::class.java).fromElements(E::class.java)` | 声明汇总结果类型与扇出元素类型 |
| `.withGenerators(listOf { ctx -> ... })` | 并行生成器；`SupplierActionContext` 即 `ActionContext`，可 `ctx.ai()` 调模型 |
| `.consolidatedBy { ctx -> ... }` | 汇总函数，`ctx.input.results` 拿到全部扇出结果 |
| `ConsensusBuilder.returning(R::class.java).withSources(...).withConsensusBy { ... }` | 多来源共识 |
| `buildAgent(name, description)` | 直接产出可注册的 `Agent`（内部已含动作与目标） |
| `agentPlatform.deploy(agent)` | 注册到平台 |

## 接口

```bash
# 并行扇出 + 汇总
curl -G --data-urlencode "topic=为什么要用 Agent 框架" http://localhost:8906/workflows/scatter-gather

# 多模型共识
curl -G --data-urlencode "question=微服务架构适合什么规模的团队？" http://localhost:8906/workflows/consensus
```

共识结果会带 `sources: ["fast","deep"]`。

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-workflows spring-boot:run
```

## 代码结构（Kotlin）

- `WorkflowConfig.kt` — 用两个 builder 构建 Agent，并用 `ApplicationRunner` 注册到平台
- `WorkflowController.kt` — 两个端点
- `Domain.kt` — 数据类（Topic/Idea/Article/Question/Answer）

## 要点

- **Kotlin 版本要对齐**：Embabel 1.0.0 的 Kotlin metadata 是 **2.1.0**，而 Spring Boot 3.5 默认把
  `kotlin.version` 管到 1.9.25。所以本模块显式覆盖为 `2.1.0`，否则编译报
  "Module was compiled with an incompatible version of Kotlin"。
- 生成器里 `ctx.last(Topic::class.java)` 从共享上下文取输入——工作流的"输入"是普通领域对象。
- 这两个原语在**纯 Java** 里也能用（生成器参数是 `java.util.function.Function`，
  `consolidatedBy` 是 Kotlin 函数类型、Java 侧用 `Function1` lambda），但 Kotlin 写起来最顺。
- 同类原语还有 `RepeatUntilAcceptable`（自评迭代，见 [embabel-refinement](../embabel-refinement/README.md)
  的 Java 手写版）与 `RepeatUntil`。
