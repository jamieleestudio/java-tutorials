# embabel-otel — 可观测性接入点（AgentInstrumentation → Micrometer）

## 演示内容

框架核心**默认不产生任何 span**。`AgentPlatformConfiguration` 注册的是
`NoOpAgentInstrumentation`，源码注释原话：

> *"the core creates no span until an observability module contributes an
> `AgentInstrumentation` adapter (registered `@Primary`)"*

所以"接入 OpenTelemetry"在 Embabel 里就是**提供一个 `@Primary AgentInstrumentation` Bean**。
本模块用 Micrometer 的 `ObservationRegistry` + 自定义 handler 把这条路径跑通，
并把 span 收集起来给你看。

## 实测：一次运行产生 11 个 span（三层）

```
spanCount=11
  http.client.requests      2705.82ms  tags={client.name=api.deepseek.com, method=POST, status=200, uri=/chat/completions}
  gen_ai.client.operation   2739.06ms  tags={gen_ai.operation.name=chat, gen_ai.request.model=deepseek-flash, gen_ai.system=openai}
  embabel.operation         3038.44ms  tags={}
  embabel.operation         3039.48ms  tags={}
  embabel.operation         3095.36ms  tags={}
  http.client.requests      3484.76ms  ...
  gen_ai.client.operation   3487.62ms  ...
  embabel.operation         3489.32ms  tags={}
  embabel.operation         3489.63ms  tags={}
  embabel.operation         3493.70ms  tags={}
  embabel.operation         6608.35ms  tags={}      ← 整个进程
```

**三层 span 都自动就有了**：

| span 名 | 来源 | 价值 |
|---|---|---|
| `embabel.operation` | 框架的 `AgentInstrumentation`（本模块接上后才出现） | 进程/动作级耗时 |
| `gen_ai.client.operation` | **GenAI 语义约定**（`gen_ai.request.model` 等标准标签） | 模型调用可跨工具聚合 |
| `http.client.requests` | Spring 的 HTTP 客户端观测 | 真实网络耗时（含 200/错误状态） |

`gen_ai.*` 那层特别有用：它用的是业界 GenAI 语义约定标签，
意味着你在 Jaeger/Grafana 里可以直接按**模型名**聚合所有调用，不需要自己定义标签。

## 关键 API

| API | 作用 |
|---|---|
| `AgentInstrumentation.observe(Function0<Context>, Function0<T>)` | 框架的唯一接入点：把 context 交给 Observation |
| `@Bean @Primary AgentInstrumentation` | 覆盖框架的 no-op 实现 |
| `ObservationRegistry` + `ObservationHandler<Context>` | Micrometer 侧：收集/导出 span |
| `Observation.createNotStarted(name, contextSupplier, registry).observe(work)` | 与框架内部 `Observations.observeOrSkip` 同样的写法（用公开 API） |
| `AgentPlatformConfiguration.agentInstrumentation()` | 框架注册的 no-op Bean（名字就叫 `agentInstrumentation`） |

## ⚠️ 两个必须知道的坑

### 1. Bean 名不能叫 `agentInstrumentation`

框架已经用这个名字注册了 no-op 版本，**同名会直接启动失败**：

```
The bean 'agentInstrumentation' ... could not be registered.
A bean with that name has already been defined in class path resource
[com/embabel/agent/spi/config/spring/AgentPlatformConfiguration.class]
```

正确做法：**换个 Bean 名 + `@Primary`**（按类型注入时 `@Primary` 胜出）。
本模块实测如此，第一次踩坑后修正。

### 2. 泛型签名必须与接口一致

`AgentInstrumentation.observe` 声明的是 `Function0<? extends Context>` / `Function0<? extends T>`。
Java 实现时如果写成 `Function0<Context>` / `Function0<T>`，编译会报
*"same erasure, yet neither overrides the other"* —— 必须原样带 `? extends`。

## 接口

```bash
# 跑一次 Agent 并返回本次 span
curl http://localhost:8942/observability/run

# 只读当前收集到的 span
curl http://localhost:8942/observability/spans
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-otel spring-boot:run
```

## 代码结构

- `InstrumentationConfig.java` — `ObservationRegistry` + `@Primary AgentInstrumentation`（核心，约 20 行）
- `SpanRecorder.java` — 把每个 `Observation` 记成一条可查询的 span（生产上换成 OTLP exporter）
- `TracedAgent.java` — 一个两步 Agent，**没有任何可观测性代码**
- `OtelController.java` — `/observability/run`、`/observability/spans`
- `SpanReport.java` — span 列表（名称、耗时、是否出错、标签）

## 要点

- **Agent 代码零改动**：`TracedAgent` 里没有一行埋点代码。可观测性是**平台级**能力，
  这正是 `AgentInstrumentation` 这个 port 的设计意图。
- **换后端不改代码**：本模块的 `SpanRecorder` 换成 `micrometer-tracing-bridge-otel` +
  OTLP exporter，span 就会进 Jaeger/Tempo。框架只依赖 `Observation` 抽象。
- **span 名由 `ObservationConvention` 决定**：框架传的是占位名 `embabel.operation`
  （`Observations.PLACEHOLDER_NAME`），真实名字由 observability 模块注册的 convention 命名——
  所以核心代码里**没有硬编码任何遥测名称**，这是很克制的设计。
- **和 `embabel-observability` 的分工**：那个模块用 `AgenticEventListener` 做**事件级**观测
  （成本、token、动作历史，业务语义强）；本模块用 `AgentInstrumentation` 做**span 级**观测
  （耗时、调用链，适合接入标准 APM）。两者互补，生产上都值得接。
- 生产建议：把 `AgentInstrumentation` 接 OTLP 之后，再用 `embabel-budget` 的成本数据
  给 span 加成本标签，就能做到"按租户/按模型看成本 + 按调用链看瓶颈"。
