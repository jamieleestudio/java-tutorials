# agentscope-streaming — 流式输出

## 演示内容

用 `agent.stream(msgs, StreamOptions, RuntimeContext)` 返回 `Flux<Event>`，每个事件携带一小段增量。

**两个端点**：
- `GET /stream/text` — 只返回文本增量（纯 String SSE）
- `GET /stream/events` — 返回完整事件流（`[REASONING]` / `[SUMMARY]` / `[AGENT_RESULT]` 等类型标注）

实测 `/stream/events`（628 行事件，可见思考→回复的分阶段）：

```
data:[REASONING]          ← 思考阶段（大量小块）
data:[REASONING]
...
data:[AGENT_RESULT] AgentScope 是阿里通义实验室开源的多智能体应用开发框架...
```

## 关键 API

| API | 作用 |
|---|---|
| `agent.stream(List<Msg>, StreamOptions, RuntimeContext)` | 返回 `Flux<Event>` |
| `Event.getType()` | `REASONING` / `SUMMARY` / `TOOL_RESULT` / `AGENT_RESULT` |
| `Event.getMessage()` | 携带 `Msg`，含 `TextBlock` / `ThinkingBlock` 等 |
| `StreamOptions.defaults()` | 默认流式选项（可配 incremental / includeReasoningChunk 等） |
| `Msg.getFirstContentBlock(TextBlock.class)` | 从 Msg 中取文本块 |

## 与 Embabel 的对照

| | Embabel | AgentScope |
|---|---|---|
| 流式 | `runner.streaming().withPrompt(msg).generateStream()` → `Flux<String>` | `agent.stream(msgs, options, ctx)` → `Flux<Event>` |
| 粒度 | 纯文本增量 | **事件级**：区分 REASONING / SUMMARY / TOOL_RESULT / AGENT_RESULT |
| 事件类型 | 无 | 6 种 EventType + 30+ 具体事件类（TextBlockDelta / ThinkingBlockDelta / ToolCallDelta 等） |

**AgentScope 独有**：事件流比纯文本流**细粒度得多**——前端可以分别渲染思考过程、工具调用、最终回复，
而不是把它们混在一起。这是 AgentScope 的"Event System"设计的直接体现。

## 代码结构

- `StreamingAgent.java` — `agent.stream()` + `streamText()`（过滤为纯文本）
- `StreamingController.java` — `GET /stream/text`、`GET /stream/events`
- 用了 `spring-boot-starter-webflux`（SSE 需要 Reactor 支持）

## 要点

- `agent.stream()` 的第一个参数是 `List<Msg>`（不是单个 `Msg`）——这是因为流式可能需要带上历史消息。
  单条消息用 `List.of(new UserMessage(msg))` 包装。
- `StreamOptions` 可以控制：流式输出某些 EventType、是否包含 thinking chunk、是否增量等。
  本模块用 `StreamOptions.defaults()`，后续 Middleware 模块会展示自定义 StreamOptions。