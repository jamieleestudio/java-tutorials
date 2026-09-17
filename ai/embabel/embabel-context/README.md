# ② 上下文工程（embabel-context）

## 这一章解决什么

**模型看到什么**决定了它答得好不好。这一章讲"如何把知识/文件/向量喂给模型"，
以及如何在成本与准确率之间取舍。

## 模块清单

| 模块 | 端口 | 主题 |
|---|---|---|
| [embabel-references](../embabel-context/embabel-references/README.md) | 8901 | 引用加载（`LlmReference`，轻量 RAG：把文档全量注入提示词） |
| [embabel-file-tools](../embabel-context/embabel-file-tools/README.md) | 8902 | 沙箱文件工具（`FileTools.readOnly/readWrite`） |
| [embabel-embeddings](../embabel-context/embabel-embeddings/README.md) | 8907 | 嵌入与语义检索（`EmbeddingService` + 余弦相似度；需 Docker） |
| [embabel-vector-store](../embabel-context/embabel-vector-store/README.md) | 8933 | pgvector 持久化向量检索（HNSW + 元数据过滤 + 阈值 + **两阶段重排**；需 Docker） |
| [embabel-document-ingest](../embabel-context/embabel-document-ingest/README.md) | 8937 | 文档摄入流水线（md/txt/pdf → 段落感知分块 → 嵌入 → 增量；需 Docker） |
| [embabel-memory](../embabel-context/embabel-memory/README.md) | 8945 | 跨会话长期记忆（按用户召回偏好/事实，需 Docker） |

## 建议阅读顺序

1. `embabel-references` —— 先理解"参考资料如何进入提示词"，以及"内容全量注入"的代价
2. `embabel-embeddings` —— 知识变大后，改成"只注入最相关的片段"（内存版）
3. `embabel-vector-store` —— 语料再大就得上向量库（持久化 + HNSW + SQL 过滤）
4. `embabel-document-ingest` —— 把**真实文档**喂进去：分块策略、PDF 抽取、增量摄入
5. `embabel-file-tools` —— 让模型自己去读文件（而不是把内容塞进提示词）

## 与相邻分类的边界

- **`embabel-embeddings` vs `embabel-vector-store` vs `embabel-document-ingest`**：
  依次是"内存检索（几十条）"→"向量库检索（百万级）"→"**怎么把语料放进去**"。
  前两者关注检索，第三者关注摄取。
- **`embabel-vector-store` vs `embabel-document-ingest` 的表名不能相同**：
  两者共用同一个 Postgres 库，若都用 `doc_chunk`，`CREATE TABLE IF NOT EXISTS` 会静默跳过，
  随后在旧结构上建索引就失败（本模块踩过）。所以后者用 `ingest_*` 前缀。
- **协议化的外部上下文**（如 MCP 提供的文件/检索工具） → 见 ⑦ `embabel-mcp`。
- **长期记忆（跨会话记住用户）** → 待补（`embabel-memory`）；当前可用
  `embabel-conversation`（会话内记忆）+ `embabel-persistence`（上下文落库）。
- **多模态输入（图片/PDF）** → 见 ③ `embabel-multimodal`。
- 这几者的共同点：都在回答"**给模型看什么**"；区别是**注入方式**（全量 / 检索 / 工具取用）。
