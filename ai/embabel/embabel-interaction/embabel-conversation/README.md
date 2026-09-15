# embabel-conversation — 多轮对话

## 演示内容

带记忆的多轮对话：同一 `sessionId` 下，每轮把**完整历史**交给模型，
并通过系统提示词固定助手人格。

## 关键 API

| API | 作用 |
|---|---|
| `InMemoryConversation` | Embabel 的内存会话实现（`Conversation` 接口） |
| `new UserMessage(text)` / `new AssistantMessage(...)` | 消息模型 |
| `conversation.addMessage(msg)` / `getMessages()` | 追加 / 读取历史 |
| `promptRunner.respond(List<Message>)` | 以消息列表形式发起对话 |
| `promptRunner.withSystemPrompt(...)` | 固定人格/系统提示 |

## 接口

```bash
# 第 1 轮
curl -X POST --data-urlencode "message=我叫小明，养了一只猫，叫煤球" \
     http://localhost:8897/chat/demo-1
# 第 2 轮（同一 sessionId，能记住上文）
curl -X POST --data-urlencode "message=我叫什么？我养了什么宠物？它叫什么？" \
     http://localhost:8897/chat/demo-1
# 清空会话
curl -X POST http://localhost:8897/chat/demo-1/reset
```

第 2 轮实测回复：`你叫小明，养了一只猫，叫煤球。`

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-conversation spring-boot:run
```

## 代码结构

- `ChatController.java` — 用 `Map<sessionId, InMemoryConversation>` 保存会话，每轮 `respond(history)`
- `ChatResult.java` — 单轮结果（会话 ID / 回复 / 消息总数）

## 要点

- 会话历史是**上下文长度**的主要来源，生产环境应配合窗口化/摘要（框架提供
  `WindowingConversationFormatter`、`TokenBudgetConversationFormatter`）。
- 进阶：`AgentProcessChatbot` 可以让一个常驻的 AgentProcess 托管会话，
  天然支持 HITL、工具、事件（本模块用更轻量的方式专注演示"记忆"）。
