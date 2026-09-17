# ⑥ 记忆与知识（agentscope-memory-rag）

## 这一章解决什么

AgentScope 的三层记忆 + RAG 知识检索——让 Agent 记住对话、积累知识、跨会话学习。

## 模块清单

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-memory](agentscope-memory/README.md) | 9124 | 对话记忆（MemoryConfig + MemoryFlushMiddleware） | `GET /memory/ask` |
| [agentscope-longterm-memory](agentscope-longterm-memory/README.md) | 9125 | 长期记忆（LongTermMemory + StaticLongTermMemoryHook） | `GET /longterm/ask` |
| [agentscope-rag](agentscope-rag/README.md) | 9126 | RAG 检索（Knowledge + KnowledgeRetrievalTools） | `GET /rag/ask` |
| [agentscope-rag-backends](agentscope-rag-backends/README.md) | 9127 | RAG 后端选择（InMemory/Qdrant/Milvus/Chroma） | `GET /rag/backends` |
| [agentscope-persistence](agentscope-persistence/README.md) | 9128 | 会话持久化（AgentStateStore） | `GET /persistence/chat` |

## 三层记忆

| 层 | 存储 | 用途 |
|---|---|---|
| 短期 | AgentState（内存） | 当前对话的消息历史 |
| 中期 | memory.md（文件） | 对话摘要（MemoryFlushMiddleware 自动生成） |
| 长期 | LongTermMemory（接口） | 跨会话知识（自定义实现或向量数据库） |

## RAG 模式

| 模式 | 说明 |
|---|---|
| `GENERIC` | Hook 自动检索，注入系统提示 |
| `AGENTIC` | Agent 通过工具主动检索 |
| `NONE` | 不启用 RAG |