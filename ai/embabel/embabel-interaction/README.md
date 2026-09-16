# ③ 交互与协作（embabel-interaction）

## 这一章解决什么

Agent 与**人**、与**前端**怎么交互：什么时候停下来等人、怎么把结果实时推出去、
怎么记住多轮对话、怎么接收图片。

## 模块清单

| 模块 | 端口 | 主题 |
|---|---|---|
| [embabel-hitl](../embabel-interaction/embabel-hitl/README.md) | 8894 | 人机协同（`WaitFor.confirmation` / `formSubmission`，暂停与恢复） |
| [embabel-streaming](../embabel-interaction/embabel-streaming/README.md) | 8895 | SSE 流式输出（`AiBuilder` + `Flux`） |
| [embabel-conversation](../embabel-interaction/embabel-conversation/README.md) | 8897 | 多轮对话与人格（`Conversation` + `respond(history)`） |
| [embabel-multimodal](../embabel-interaction/embabel-multimodal/README.md) | 8908 | 图像理解（`AgentImage` + `MultimodalContent`；需 Docker） |

## 建议阅读顺序

1. `embabel-conversation` —— 先有"多轮"的上下文
2. `embabel-streaming` —— 让用户看到"正在生成"
3. `embabel-hitl` —— 关键节点停下来等人工确认
4. `embabel-multimodal` —— 输入扩展到图片

## 与相邻分类的边界

- **HITL 是"交互"**（等人输入）；**⑧ patterns 里的 `embabel-trigger`** 是"动作触发时机"，
  两者常一起用但关注点不同。
- **常驻会话（多轮 + 主动发起）** 待补 `embabel-chatbot`（`AgentProcessChatbot` + `ChatSession`）；
  当前 `embabel-conversation` 是轻量实现。
- **工具级暂停/向用户索取强类型输入** 待补 `embabel-hitl-advanced`。
- **A2A（Agent 之间交互）** → 见 ⑦ `embabel-a2a`。
