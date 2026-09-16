# embabel-vector-store — pgvector 持久化向量检索

## 演示内容

RAG 的"检索"环节，但用**真正的向量库**（Postgres + pgvector）而不是内存里算余弦相似度：

1. **持久化**：向量写在 Postgres 里，进程重启不丢，不需要每次请求重算整份语料
2. **HNSW 索引**：`CREATE INDEX ... USING hnsw (embedding vector_cosine_ops)`，支持大规模近邻搜索
3. **元数据过滤**：`WHERE source = ?` 与向量排序**组合在一条 SQL 里**——内存余弦做不到
4. **相似度阈值**：低于阈值直接丢弃；全被丢弃就回答"知识库中未提及"，不硬编答案
5. **两阶段检索（召回 + 重排）**：向量粗召回 N 条 → LLM 按相关性精排 → 取前 k 条（见下文实测）

## 与 `embabel-embeddings` 的区别

| | `embabel-embeddings` | `embabel-vector-store` |
|---|---|---|
| 存储 | 内存（每次请求重算） | Postgres + pgvector（预写入） |
| 检索 | 全量扫描 + 余弦 | HNSW 近邻 + SQL 过滤 |
| 规模 | 适合几十条 | 适合百万级 |
| 依赖 | LiteLLM + Ollama | 再加 Postgres（pgvector 镜像） |

两者都是"嵌入 + 检索"，建议按语料规模选择。

## 关键 API / SQL

```java
// 写入（幂等）
jdbc.update("""
    INSERT INTO doc_chunk (id, title, content, source, embedding)
    VALUES (?, ?, ?, ?, ?::vector)
    ON CONFLICT (id) DO UPDATE SET ...
    """, ...);

// 检索：<=> 余弦距离、<-> L2、<#> 内积；1 - 距离 = 相似度
jdbc.query("""
    SELECT id, title, content, source, 1 - (embedding <=> ?::vector) AS score
    FROM doc_chunk
    WHERE source = ?                      -- 元数据过滤，与向量排序可组合
    ORDER BY embedding <=> ?::vector
    LIMIT ?
    """, ...);
```

要点：`?::vector` —— pgvector 支持从文本字面量 `[0.1,0.2,...]` 转换，所以参数直接传字符串。

## 前置条件

```bash
cd ai/embabel/docker
docker compose up -d postgres litellm ollama
docker compose exec ollama ollama pull nomic-embed-text

# DeepSeek 没有 embedding 接口，必须把这两个变量指向 LiteLLM
# PowerShell:
$env:OPENAI_BASE_URL="http://localhost:4000"; $env:OPENAI_API_KEY="sk-1234"
```

> Postgres 用的是 **`pgvector/pgvector:pg16`** 镜像（基于 postgres:16，数据目录兼容）。
> `docker/postgres/init/01-vector.sql` 会在数据库初始化时执行 `CREATE EXTENSION vector`；
> 应用启动时也会再执行一次 `CREATE EXTENSION IF NOT EXISTS vector`。

## 接口与实测

```bash
# 1) 把内置语料写入向量库（幂等，可重复调用）
curl -X POST http://localhost:8933/vector/ingest
# {"ingested":8,"total":8,"dim":768}

# 2) 原始近邻检索（看得到相似度）
curl -G --data-urlencode "q=pgvector 余弦距离怎么写" --data-urlencode "k=3" http://localhost:8933/vector/search

# 3) 元数据过滤：只在该来源里检索
curl -G --data-urlencode "q=怎么降低 RAG 成本" --data-urlencode "k=3" \
     --data-urlencode "source=internal-wiki" http://localhost:8933/vector/search

# 4) RAG：检索 + 生成（带阈值与出处）
curl -G --data-urlencode "question=pgvector 里怎么算余弦相似度？" http://localhost:8933/vector/ask

# 5) 状态
curl http://localhost:8933/vector/stats
```

实测 1（无过滤，问题关于 pgvector）——`d8` 正确排第一：

```
d8  pgvector 使用要点        score=0.785
d3  GOAP 规划器              score=0.529
d6  RAG 的成本控制           score=0.481
```

实测 2（`source=internal-wiki`，问题关于 RAG 成本）——**过滤生效且排序变化**：

```
d6  RAG 的成本控制           score=0.808   ← 加了过滤后跃居第一
d7  向量检索的召回与重排      score=0.552
d8  pgvector 使用要点        score=0.471
```

实测 3（RAG 回答，模型主动指出片段不足）：

```
根据 [internal-wiki/d8]，pgvector 中 `<=>` 表示余弦距离……
但这些片段没有直接说明"余弦相似度"如何计算，也没有给出从余弦距离转换为
余弦相似度的公式。因此，仅依据提供的知识片段，无法完整回答……
```

## 两阶段检索（召回 + 重排）

**为什么需要**：向量相似 ≠ 任务相关。实测（问题"只用向量召回有什么问题"，粗召回 6 条）：

| 顺序 | 纯向量召回（余弦相似度） | 完整重排后（LLM 相关性） |
|---|---|---|
| 1 | d4 黑板与共享上下文 `0.635` | **d7 向量检索的召回与重排 `1.00`** |
| 2 | d2 类型化建模 `0.611` | d6 RAG 的成本控制 `0.30` |
| 3 | d1 什么是 Agent `0.610` | d4 `0.00` |
| 4 | d6 RAG 的成本控制 `0.598` | d2 `0.00` |
| 5 | d5 工具循环与停止条件 `0.579` | d1 `0.00` |
| 6 | **d7 向量检索的召回与重排 `0.570`** | d5 `0.00` |

正确片段 `d7` 在余弦排序里是**最后一名**，重排后跃到**第一**。
（两者 score 语义不同——余弦相似度 vs LLM 相关性——看的是**顺序变化**。）

```bash
# 对照：同一批候选的两种顺序
curl -G --data-urlencode "question=只用向量召回有什么问题" --data-urlencode "recall=6" \
     http://localhost:8933/vector/compare

# 用重排后的 top-k 作为上下文生成回答
curl -G --data-urlencode "question=只用向量召回有什么问题" --data-urlencode "k=3" \
     http://localhost:8933/vector/ask-reranked
# {"recalled":8,"usedChunks":["d7","d6","d8"],
#  "answer":"只用向量召回（只做粗召回、不做精排）时，top-1 常常不是最相关的…[internal-wiki/d7]"}
```

### ⚠️ 框架自带的 `Ranker` 是"选择器"，不是"重排器"

读了 `LlmRanker` 的实现后确认：它的提示词是
*"choose the name that best reflects the user's intent"*（选**一个**），
所以它返回的通常只是最相关的那条，而不是全部候选的新顺序；
而且它要求模型返回的名字**严格存在于候选列表**，否则直接抛
`IllegalStateException`（模型写错一个 id 就整次失败）。

那个语义适合"从若干目标里挑一个"（见 `embabel-multi-goal`），**不适合**"把 8 条候选重新排序"。
所以本模块自己实现重排：一次调用给**所有**候选打分，缺 id 的按 0 分兜底。
生产上更常见的是用 **cross-encoder** 模型（更准，但要额外部署模型服务）。

## 数据库侧验证

```
$ docker exec embabel-postgres psql -U embabel -d embabel -c "\dx" -c "\d doc_chunk"
  Name   | Version | ...
 vector  | 0.8.6   | vector data type and ivfflat and hnsw access methods

              Table "public.doc_chunk"
  Column   |    Type     |
 id        | text        |
 title     | text        |
 content   | text        |
 source    | text        |
 embedding | vector(768) |
Indexes:
    "doc_chunk_pkey" PRIMARY KEY, btree (id)
    "doc_chunk_embedding_idx" hnsw (embedding vector_cosine_ops)
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-vector-store spring-boot:run
```

## 代码结构

- `PgVectorStore.java` — DDL（扩展/表/HNSW 索引）+ upsert + 带过滤的近邻检索
- `VectorStoreAgent.java` — 检索 + 阈值 + 生成（目标类型 `VectorAnswer`）
- `Reranker.java` — **两阶段检索的第二阶段**：让 LLM 给所有候选打分并重排
- `RerankScores` / `RerankComparison` — 重排评分与对照结果
- `VectorStoreController.java` — `/vector/ingest`、`/vector/search`、`/vector/ask`、`/vector/stats`、`/vector/compare`、`/vector/ask-reranked`
- `DocChunk.java` — 内置语料（8 条，两个来源）
- `VectorMatch` / `VectorAnswer` — 命中结果与回答（`VectorMatch` 实现 `Named`+`Described`）

## 要点

- **`app.vector.dim` 必须与嵌入模型维度一致**（nomic-embed-text = 768）。
  不一致时 `PgVectorStore` 会在写入时报错并提示，而不是静默写入坏数据。
- 直接用 **JDBC + 原生 SQL**，没有用 Spring AI 的 `VectorStore` 抽象——
  这样能看清 pgvector 的真实用法（距离算子、`::vector` 转换、HNSW 索引、SQL 过滤），
  也少一层 autoconfiguration 依赖。
- **HNSW 需要 pgvector ≥ 0.5**（本机镜像实测 0.8.6）。数据量很小时 HNSW 收益不明显，
  但索引存在与否决定了能否平滑扩到百万级。
- `<=>` 是**余弦距离**，所以相似度 = `1 - 距离`；注意别把距离当相似度用（越大越不相关）。
- 生产上还应考虑：分块策略（见 `embabel-document-ingest`）、批量写入（`COPY`/多值 `INSERT`）、
  以及**重排**的成本收益（多一次 LLM 调用，但能显著缩小最终上下文）。
- **重排的收益有多大**：实测把正确片段的排序从第 6 名提到第 1 名。
  如果只取 top-1，纯召回会拿到完全不相关的内容，重排后拿到正确答案——
  这是 RAG 里投入产出比最高的一步之一。
