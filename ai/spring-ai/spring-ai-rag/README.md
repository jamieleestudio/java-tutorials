# ④ RAG 与知识库（spring-ai-rag）

## 这一章解决什么

嵌入 → 向量库 → 检索增强生成，三段式 RAG 管道。

## 模块清单

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [spring-ai-embedding](./spring-ai-embedding/README.md) | 8010 | 向量嵌入 | `GET /ai/embedding` |
| [spring-ai-vector-store](./spring-ai-vector-store/README.md) | 8011 | 向量存储 | `GET /ai/vector/search` |
| [spring-ai-rag-basics](./spring-ai-rag-basics/README.md) | 8012 | RAG 检索 | `GET /ai/rag` |
