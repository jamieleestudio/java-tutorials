# ③ 交互与协作（embabel-interaction）

## 这一章解决什么

Agent 与**人**、与**前端**怎么交互：什么时候停下来等人、怎么把结果实时推出去、
怎么记住多轮对话、怎么接收图片。

## 模块清单

| 模块 | 端口 | 主题 |
|---|---|---|
| [embabel-hitl](../embabel-interaction/embabel-hitl/README.md) | 8894 | 人机协同（`WaitFor.confirmation` / `formSubmission`，暂停与恢复） |
| [embabel-hitl-advanced](../embabel-interaction/embabel-hitl-advanced/README.md) | 8938 | 工具级 HITL（`ConditionalAwaitingTool` + `AwaitDecider` + `TypeRequest`，按需索要强类型输入） |
| [embabel-conversation](../embabel-interaction/embabel-conversation/README.md) | 8897 | 多轮对话 + SSE 流式 + **Web UI**（打开 `http://localhost:8897/`；`Conversation` + `respond(history)` / `Flux`） |
| [embabel-multimodal](../embabel-interaction/embabel-multimodal/README.md) | 8908 | 图像理解（`AgentImage` + `MultimodalContent`；需 Docker） |

## 建议阅读顺序

1. `embabel-conversation` —— 先有"多轮"的上下文；再看同模块的**流式**端点（让用户看到"正在生成"）
2. `embabel-hitl` —— 关键节点停下来等人工确认
3. `embabel-hitl-advanced` —— 把等待下沉到**工具调用点**，并只在需要时才打扰用户
4. `embabel-multimodal` —— 输入扩展到图片

## 与相邻分类的边界

- **HITL 是"交互"**（等人输入）；**⑧ patterns 里的 `embabel-trigger`** 是"动作触发时机"，
  两者常一起用但关注点不同。
- **多轮与流式放在同一模块**：两者共享同一份会话历史（`ConversationStore`），
  可以先用流式说一句、再用阻塞接口追问，模型仍然记得上文——分开会切断这个演示。
- **常驻会话（多轮 + 主动发起）** 待补 `embabel-chatbot`（`AgentProcessChatbot` + `ChatSession`）；
  当前 `embabel-conversation` 是轻量实现。
- **工具级暂停/向用户索取强类型输入** → 见 `embabel-hitl-advanced`（`TypeRequest` 的 payload 是类型本身，
  所以类型定义即表单 schema；注意它的动作必须 `canRerun = true`）。
- **A2A（Agent 之间交互）** → 见 ⑦ `embabel-a2a`。
