# ④ RAG（spring-ai-alibaba-rag）

## 这一章解决什么

检索增强三件套：嵌入 → 向量库 → 三段式 RAG。嵌入默认走 DashScope 兼容模式（OpenAI 兼容协议），聊天走 DeepSeek。

## 模块清单

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [embedding](./spring-ai-alibaba-embedding/README.md) | 8514 | 向量嵌入 | `GET /ai/embedding` |
| [vector-store](./spring-ai-alibaba-vector-store/README.md) | 8515 | 向量存储 | `GET /ai/vector/search` |
| [rag-basics](./spring-ai-alibaba-rag-basics/README.md) | 8516 | RAG 三段式 | `GET /ai/rag` |
