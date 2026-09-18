# ③ Advisor 链（spring-ai-advisors）

## 这一章解决什么

Advisor 是 Spring AI 的中间件：日志、安全护栏、对话记忆。

## 模块清单

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [spring-ai-advisor-basics](./spring-ai-advisor-basics/README.md) | 8007 | Advisor 基础 | `GET /ai/advisor` |
| [spring-ai-safeguard](./spring-ai-safeguard/README.md) | 8008 | 安全护栏 | `GET /ai/safeguard` |
| [spring-ai-chat-memory-advisor](./spring-ai-chat-memory-advisor/README.md) | 8009 | 对话记忆 | `GET /ai/memory` |
