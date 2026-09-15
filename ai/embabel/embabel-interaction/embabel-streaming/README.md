# embabel-streaming — 流式输出（SSE）

## 演示内容

把模型输出以 Server-Sent Events 逐块推送给前端。演示如何在 Agent/动作之外，
通过注入 `AiBuilder` 直接拿到 `Ai` 网关，再拿到支持流式的 `PromptRunner`。

## 关键 API

| API | 作用 |
|---|---|
| `AiBuilder`（Spring Bean，可注入） | `aiBuilder.ai()` 在动作之外获取 `Ai` |
| `promptRunner.supportsStreaming()` | 先探测底层模型是否支持流式 |
| `promptRunner.streaming()` | 返回 `StreamingPromptRunner.Streaming` |
| `.withPrompt(...).generateStream()` | 返回 `Flux<String>`，逐块吐出 |
| `produces = MediaType.TEXT_EVENT_STREAM_VALUE` | Spring MVC 以 SSE 形式输出 Flux |

## 接口

```bash
curl -N -G --data-urlencode "message=用三句话介绍 Embabel" http://localhost:8895/stream/generate
```

输出为多条 `data:...` 事件（DeepSeek 下实测约 90+ 个 chunk）。

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-streaming spring-boot:run
```

## 代码结构

- `StreamingController.java` — `GET /stream/generate`，返回 `Flux<String>`

## 要点

- **先判断再调用**：`supportsStreaming()` 为 false 时本示例降级为一次性返回，避免抛异常。
- thinking 提取与 streaming 两个能力**互斥**（见 [embabel-thinking](../../embabel-reasoning/embabel-thinking/README.md)）。
- 框架还内置了进程事件 SSE 端点 `/events/process/{id}`，可用于观测 Agent 执行过程。
- Spring MVC 直接支持返回 `Flux`（Reactor 已在 classpath 上），无需引入 WebFlux。
