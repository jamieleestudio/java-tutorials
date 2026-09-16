# embabel-parallelization — 并行化（两种写法对照）

## 演示内容

同一个"并行化"模式，用**两种写法**各实现一遍，方便对照：

| 端点 | 模式 | 写法 |
|---|---|---|
| `GET /parallel/sectioning` | **Sectioning**（分片） | Java：多个独立 `@Action` + 一个汇总 `@Action` |
| `GET /parallel/voting` | **Voting**（投票） | Java：同一任务多视角独立判定 + 多数票 |
| `GET /parallel/scatter-gather` | 同上 Sectioning | Kotlin：`ScatterGatherBuilder` 原语 |
| `GET /parallel/consensus` | 同上 Voting（多模型） | Kotlin：`ConsensusBuilder` 原语 |

对应 Anthropic《Building Effective Agents》里 Parallelization 的两个变体。

**为什么要两种写法都留**：手写让你看清"并发从哪来、依赖怎么连"；
原语让你看到"这些样板可以自动化"。选哪个取决于你是想控制细节还是想少写代码。

## 关键 API

### Java 手写（Sectioning / Voting）

| API | 作用 |
|---|---|
| 多个 `@Action` 只依赖 `UserInput` | 形成互不依赖的分片动作 |
| 汇总 `@Action` 同时接收多份结果 | 由规划器推导依赖，等所有分片完成 |
| `embabel.agent.platform.process-type: CONCURRENT` | **并发的来源**：允许并发执行独立动作 |
| `@AchievesGoal` | 汇总动作产出目标类型 |

### Kotlin 原语（ScatterGather / Consensus）

| API | 作用 |
|---|---|
| `ScatterGatherBuilder.returning(R).fromElements(E)` | 声明"元素 E 扇出 → 汇总为 R" |
| `.withGenerators(List<Function<SupplierActionContext<E>, E>>)` | N 个生成器（并发扇出） |
| `.consolidatedBy { ctx -> ... }` | 汇总（`ctx.input.results` 拿到全部结果） |
| `ConsensusBuilder.returning(R).withSources(...)` | 多来源各自产出 R |
| `.withConsensusBy { ctx -> ... }` | 达成共识 |
| `.buildAgent(name, description)` | 直接得到可注册的 `Agent` |
| `AgentPlatform.deploy(agent)` | 注册到平台（`ApplicationRunner` 里显式调用） |
| `ctx.ai().withLlmByRole("fast")` | 按**角色**选模型（映射见 yml 的 `embabel.models.llms`） |

## 接口与实测

```bash
curl -G --data-urlencode "message=用 Redis 缓存热点商品数据并设置 5 分钟过期" http://localhost:8918/parallel/sectioning
curl -G --data-urlencode "message=给用户发一封包含折扣码的营销邮件"        http://localhost:8918/parallel/voting
curl -G --data-urlencode "topic=为什么要用 Agent 框架而不是写死工作流"     http://localhost:8918/parallel/scatter-gather
curl -G --data-urlencode "question=微服务架构适合什么规模的团队？"         http://localhost:8918/parallel/consensus
```

Voting 实测（三票里两票认为有问题，按多数票判定）：

```json
{"decision":"有问题","votes":["有问题","没问题","有问题"],
 "rationale":"共 3 票，其中 2 票认为有问题，按多数票判定：有问题"}
```

Consensus 实测（两个模型角色各自回答后合并，`sources` 标出来源）：

```json
{"content":"微服务无硬性人数门槛，关键看业务复杂度、组织协作成本与工程成熟度...",
 "sources":["fast","deep"]}
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-parallelization spring-boot:run
```

## 代码结构

Java（手写）：
- `SectioningAgent.java` — 3 个分片动作 + 汇总动作
- `VotingAgent.java` — 3 个视角独立判定 + 多数票
- `Report` / `Verdict` / `SecurityReview` / `PerformanceReview` / `MaintainabilityReview` — 类型
- `ParallelizationController.java` — `/parallel/sectioning`、`/parallel/voting`

Kotlin（原语）：
- `WorkflowConfig.kt` — `ScatterGatherBuilder` / `ConsensusBuilder` 两个 Agent + `deploy`
- `WorkflowController.kt` — `/parallel/scatter-gather`、`/parallel/consensus`
- `Domain.kt` — `Topic` / `Idea` / `Article` / `Question` / `Draft` / `Answer`

## 要点

- **四个 Agent 的目标类型必须互不相同**（`Report` / `Verdict` / `Article` / `Answer`）：
  `AgentInvocation.create(platform, X.class)` 是按目标类型选 Agent 的，重名会命中歧义。
- `process-type: CONCURRENT` 只对手写 `@Action` 版本有意义；
  builder 版本自己管并发（`DEFAULT_MAX_CONCURRENCY`）。
- **混合语言模块**：本模块同时有 `src/main/java` 和 `src/main/kotlin`。
  pom 里保留默认 java sourceDirectory，另用 `kotlin-maven-plugin` 的 `<sourceDirs>` 编译 kotlin，
  两侧输出到同一 `target/classes`。两侧互不引用，所以编译顺序无关。
- Kotlin 支持**嵌套块注释**——注释里写路径时别出现 `/*`（会打开嵌套注释），
  本模块第一版就因此报过 `Unclosed comment`。
