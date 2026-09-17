# embabel-web-ui — 最小 Web UI（静态页 + SSE）

## 演示内容

48 个模块之前都只能 curl。这个模块让它们**在浏览器里可点**：
一个静态 HTML 页面 + 一个 SSE 流式端点，**没有前端构建步骤**（纯 vanilla JS）。

```bash
cd ai/embabel
mvn -pl :embabel-web-ui spring-boot:run
# 打开 http://localhost:8943/
```

页面功能：流式对话、3 种人格（系统提示词）切换、清空会话、其他模块端口速查。

## 实测

```
GET /                                    -> 200, 6754 bytes, <title>Embabel 教程 · 最小 Web UI</title>
GET /ui/chat/stream?message=...          -> data:Emb / data:abel / data: 是 / data:一个 / ...（逐块）
POST /ui/chat (同一 sessionId 追问)      -> {"messageCount":4,"reply":"你刚才问的是：\"用一句话说明 Embabel 是什么\"。"}
```

最后一条是**关键验证**：先走流式说了"用一句话说明 Embabel 是什么"，
再用**阻塞式**接口追问"我刚才问的是什么"，模型答对了 ——
说明流式那轮被正确写回了会话历史（`messageCount=4`）。

## 关键 API

| API | 作用 |
|---|---|
| `src/main/resources/static/index.html` | Spring Boot 自动把 `static/` 映射到 `/`（无需配置） |
| `EventSource`（浏览器） | 原生 SSE 客户端，零依赖 |
| `GET /ui/chat/stream`（`produces=TEXT_EVENT_STREAM_VALUE`） | 返回 `Flux<String>`，每块一个 `data:` 事件 |
| `StreamingPromptRunner.Streaming.withMessages(history).generateStream()` | 带历史流式 |
| `runner.supportsStreaming()` | **先探测**，不支持则降级为一次性返回 |
| `Flux.defer(...)` | 把"追加用户消息 + 读历史"推迟到订阅时刻，避免重复订阅错乱 |
| `doOnComplete(...)` | 流结束时把**拼装好的完整回复**写回历史（否则历史里留半截） |

## 代码结构

- `src/main/resources/static/index.html` — 全部前端（HTML + CSS + JS，约 180 行）
- `UiChatController.java` — `POST /ui/chat`、`GET /ui/chat/stream`、`POST /ui/reset`
- `WebUiApplication.java` — 启动类

## 要点

- **前端不打包**：没有 npm / webpack / node_modules。静态页直接由 Spring Boot 提供，
  对一个"教学用最小 UI"来说足够了，也避免了把前端工具链引进 Java 仓库。
- **SSE 用 GET + query 参数**：因为浏览器的 `EventSource` 只支持 GET。
  要传更复杂的数据（或需要 POST）就得用 `fetch` + `ReadableStream` 自己解析。
- **`data:` 里可能含换行**：SSE 规范下多行 `data:` 会被拼成一条消息，所以服务端每块尽量不带换行；
  页面里对 `\n` 做了还原处理。
- **同一个会话可混用流式与阻塞**：因为两者共享同一份历史（这个模式在
  `embabel-conversation` 里也是重点）。
- **生产上要补的东西**：鉴权、会话持久化（见 `embabel-persistence`）、
  多用户隔离（见 `embabel-identity`）、断线重连与取消（`EventSource` 自带重连但不会恢复上下文）。
- 与 `embabel-conversation` 的分工：那个模块讲**机制**（`Conversation` + `respond(history)` + 流式写回），
  本模块讲**怎么让 demo 可点**（静态页 + `EventSource`），代码刻意更薄。
