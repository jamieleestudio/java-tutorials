# embabel-observability — 事件监听与成本/Token 统计

## 演示内容

监听 Agent 执行过程中的事件，输出**每步耗时、LLM 调用次数、模型、token 与成本**。
这是把 Agent 跑进生产的第一步：没有观测就无法调优与控成本。

## 关键 API

| API | 作用 |
|---|---|
| `AgenticEventListener` | 实现它并声明为 Spring Bean 即可**自动接收**事件（无需手动注册） |
| `ActionExecutionStartEvent` / `ActionExecutionResultEvent` | 动作开始/结束（含耗时、状态） |
| `LlmResponseEvent` | 每次 LLM 调用结束（含模型名、耗时） |
| `AgentProcessCompletedEvent` / `FailedEvent` / `StuckEvent` | 进程终态 |
| `AgentProcess.getHistory()` | 动作历史（名称 + 耗时） |
| `AgentProcess.totalCost()` / `totalUsage()` / `ownModelsUsed()` | 成本、token、模型 |

> 注册机制：`AgentPlatformConfiguration.eventListener(listeners: List<AgenticEventListener>)`
> 会把容器里所有 `AgenticEventListener` Bean 组合成一个多播监听器。所以**只需 `@Component`**。

## 接口

```bash
curl -G --data-urlencode "message=为什么要给 Agent 做可观测性？" http://localhost:8903/observability/run
```

返回示例：

```json
{
  "status": "COMPLETED",
  "steps": ["...outline (3919ms)", "...answer (3763ms)"],
  "cost": 0.001455,
  "totalTokens": 1523,
  "models": ["deepseek-flash"],
  "events": ["action:start ...", "llm:response model=deepseek-flash 3896ms", "process:completed"],
  "llmCalls": 2,
  "actionRuns": 2
}
```

事件同时打印在应用日志里（`[observability] ...`）。

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-observability spring-boot:run
```

## 代码结构

- `MetricsEventListener.java` — 事件监听器（按进程记录事件 + 全局计数）
- `ObservabilityAgent.java` — 两步执行（2 动作 / 2 次 LLM 调用），便于观察
- `ObservabilityController.java` — 运行并汇总报告
- `RunReport.java` — 报告结构

## 要点

- 成本来自模型清单里的 `pricing_model`（见 `models/openai-models.yml`）；价格变动时记得更新。
- 想做指标接入（Micrometer/OpenTelemetry），可在此监听器里打点，或使用框架的
  `AgentInstrumentation` 观测适配器。
- 进程级监控还有配置项 `embabel.agent.platform.rest.*` / `sse.*`，但对应的 REST/SSE 端点
  需要额外的框架模块（当前 starter 未包含），本模块用自定义监听器演示。
