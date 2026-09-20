# ① 基础（spring-ai-alibaba-basics）

## 这一章解决什么

SAA 环境下的 Spring AI 基础用法：ChatClient 高/底层 API、流式、结构化输出、多模型接入。纯 Spring AI 模块（GA 2.0.0）。

## 模块清单

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [spring-ai-alibaba-chat](./spring-ai-alibaba-chat/README.md) | 8500 | 基础聊天 | `GET /ai/chat` |
| [spring-ai-alibaba-streaming](./spring-ai-alibaba-streaming/README.md) | 8501 | 流式输出 | `GET /ai/streaming` |
| [spring-ai-alibaba-structured-output](./spring-ai-alibaba-structured-output/README.md) | 8502 | 结构化输出 | `GET /ai/structured` |
| [spring-ai-alibaba-multi-model](./spring-ai-alibaba-multi-model/README.md) | 8503 | 多模型（DeepSeek + Qwen 兼容模式） | `GET /model/ask` |
