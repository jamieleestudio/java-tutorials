# embabel-conversation — 多轮对话与流式输出

## 演示内容

交互层的两件事，且**共享同一份会话历史**：

1. **多轮对话（记忆）**：同一 `sessionId` 下，每轮把完整历史交给模型，并用系统提示词固定人格
2. **流式输出（SSE）**：边生成边推送；并且**流式也记住上下文**——流结束时把拼装好的完整回复写回历史

因为两者共用 `ConversationStore`，所以可以**混用**：先流式说一句，再用阻塞接口追问，模型仍然记得。

## 关键 API

| API | 作用 |
|---|---|
| `InMemoryConversation` | Embabel 的内存会话实现（`Conversation` 接口） |
| `new UserMessage(text)` / `new AssistantMessage(text)` | 消息模型 |
| `conversation.addMessage(msg)` / `getMessages()` | 追加 / 读取历史 |
| `promptRunner.respond(List<Message>)` | 阻塞式：以消息列表发起对话，返回 `AssistantMessage` |
| `promptRunner.withSystemPrompt(...)` | 固定人格/系统提示 |
| `promptRunner.supportsStreaming()` | **先探测**，不支持则降级 |
| `runner.streaming().withMessages(history).generateStream()` | 流式：返回 `Flux<String>` |
| `Flux.defer(...)` | 冷流延迟到订阅时再追加消息/读历史，避免重复订阅错乱 |

## 接口

```bash
# 多轮对话（第 1 轮）
curl -X POST --data-urlencode "message=我叫小明，养了一只猫，叫煤球" \
     http://localhost:8897/chat/demo-1
# 第 2 轮（同一 sessionId，能记住上文）
curl -X POST --data-urlencode "message=我叫什么？我养了什么宠物？它叫什么？" \
     http://localhost:8897/chat/demo-1
# 清空会话
curl -X POST http://localhost:8897/chat/demo-1/reset

# 流式（SSE，同一 sessionId，同样有记忆）
curl -N -G --data-urlencode "message=用三句话介绍 Embabel Agent Framework" \
     http://localhost:8897/chat/demo-2/stream
```

第 2 轮实测回复：`你叫小明，养了一只猫，叫煤球。`

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-conversation spring-boot:run
```

## 代码结构

- `ConversationStore.java` — `@Component`，按 sessionId 保存历史（两个控制器共用）
- `ChatController.java` — `POST /chat/{sessionId}`、`POST /chat/{sessionId}/reset`
- `StreamingController.java` — `GET /chat/{sessionId}/stream`（SSE，带记忆）
- `ChatResult.java` — 单轮结果（会话 ID / 回复 / 消息总数）

## 要点

- 会话历史是**上下文长度**的主要来源，生产环境应配合窗口化/摘要（框架提供
  `WindowingConversationFormatter`、`TokenBudgetConversationFormatter`）。
- **流式必须处理"不支持"的情况**：`supportsStreaming()` 为 false 时降级为一次性返回，
  否则换模型/换 Provider 就会直接抛异常。
- 流式的历史要**在流结束时**才写回（`doOnComplete`），且用 `StringBuilder` 拼装分块；
  否则历史里会留下半截回复。
- 进阶：`AgentProcessChatbot` 可以让一个常驻的 AgentProcess 托管会话，
  天然支持 HITL、工具、事件（本模块用更轻量的方式专注演示"记忆 + 流式"）。
