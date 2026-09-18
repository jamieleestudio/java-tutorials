# ① 基础（spring-ai-basics）

## 这一章解决什么

从零开始：ChatClient 高层 API + ChatModel 底层 API + 结构化输出 + 提示模板。

## 模块清单

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [spring-ai-chat](./spring-ai-chat/README.md) | 8000 | 基础聊天 | `GET /ai/chat` |
| [spring-ai-streaming](./spring-ai-streaming/README.md) | 8001 | 流式输出 | `GET /ai/streaming` |
| [spring-ai-structured-output](./spring-ai-structured-output/README.md) | 8002 | 结构化输出 | `GET /ai/structured` |
| [spring-ai-prompt-templates](./spring-ai-prompt-templates/README.md) | 8003 | 提示模板 | `GET /ai/prompt` |
