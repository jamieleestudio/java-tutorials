# embabel-document-ingest — 文档摄入流水线（分块 / 嵌入 / 增量）

## 演示内容

RAG 的**前半段**：把磁盘上的文档变成向量库里的块。

```
扫描目录 → 变更检测(内容哈希) → 段落感知分块 → 嵌入 → 写入 pgvector
```

1. **格式支持**：`.md` / `.txt` / `.pdf`（PDF 用 PDFBox 抽文本）
2. **段落感知分块**：按空行切段 → 贪心聚合到目标大小 → 相邻块**重叠**，避免答案被切在边界上
3. **增量摄入**：用内容 SHA-256 与库里的哈希比对，没变就**跳过**（嵌入是最贵的一步）
4. **可重跑**：摄入某文档时先按 `doc_id` 删旧块再写新块，改分块参数后重跑不会残留

## 与 `embabel-vector-store` 的分工

| | `embabel-vector-store` | `embabel-document-ingest` |
|---|---|---|
| 关注点 | **存储 + 检索**（DDL / HNSW / 元数据过滤 / 阈值） | **摄取**（读文件 / 分块 / 嵌入 / 增量） |
| 语料 | 内置 8 条常量 | 真实目录里的 `.md`/`.txt`/`.pdf` |
| 表名 | `doc_chunk` | `ingest_chunk` + `ingest_doc` |

两者互补：先把文档摄入（本模块），再按检索质量调优（vector-store）。

## 关键 API / 做法

| 项 | 说明 |
|---|---|
| `Loader.loadPDF(file)` + `PDFTextStripper().getText(doc)` | PDFBox 3.x 抽 PDF 文本（两步，不需要 reader 抽象） |
| `text.split("\n\\s*\n")` | 按**空行**切段（保留段落语义，而不是按字符硬切） |
| 贪心聚合到 `chunk-size` | 段落拼到接近目标大小才落一块 |
| 超长段落硬切 | 单段 > 2×目标大小时硬切，避免"一个巨块" |
| 相邻块 `chunk-overlap` | 上一块尾部接进下一块，保证跨块语义连续 |
| `SHA-256(抽出文本)` 存 `ingest_doc.content_hash` | 增量判断依据 |
| `EmbeddingService.embed(text)` | 逐块嵌入（`aiBuilder.ai().withDefaultEmbeddingService()`） |
| `1 - (embedding <=> ?::vector)` | 检索相似度（余弦距离 → 相似度） |

## 前置条件

```bash
cd ai/embabel/docker
docker compose up -d postgres litellm ollama
docker compose exec ollama ollama pull nomic-embed-text

# DeepSeek 没有 embedding 接口，必须指向 LiteLLM
$env:OPENAI_BASE_URL="http://localhost:4000"; $env:OPENAI_API_KEY="sk-1234"
```

摄入目录默认 `${java.io.tmpdir}/embabel-ingest-docs`，**首次启动会自动写入 3 个示例 `.md`**，
所以拉下来就能跑（可用 `INGEST_DIR` 覆盖）。

## 接口与实测

```bash
# 1) 生成演示 PDF 并立即摄入（验证 PDF 路径）
curl -X POST http://localhost:8937/ingest/demo-pdf

# 2) 摄入（幂等）
curl -X POST http://localhost:8937/ingest/run

# 3) 检索（看得到相似度与出处 docId/seq）
curl -G --data-urlencode "q=分块为什么要留重叠" --data-urlencode "k=3" http://localhost:8937/ingest/search

# 4) 状态
curl http://localhost:8937/ingest/stats
```

实测 `POST /ingest/demo-pdf`（3 个 md + 1 个 pdf）：

```json
{"scanned":4,"ingested":4,"skipped":0,"totalChunks":5,
 "chunking":"chunk-size=300, overlap=60, 段落感知",
 "details":[
   {"docId":"embabel-overview.md","chunks":2,"status":"INGESTED"},
   {"docId":"embabel-pdf-demo.pdf","chunks":1,"status":"INGESTED"},
   {"docId":"pgvector-notes.md","chunks":1,"status":"INGESTED"},
   {"docId":"rag-notes.md","chunks":1,"status":"INGESTED"}],
 "elapsedMillis":1983}
```

**再跑一次**——增量生效，耗时从 1983ms 降到 **13ms**：

```json
{"scanned":4,"ingested":0,"skipped":4,"totalChunks":5,
 "details":[{"docId":"embabel-overview.md","chunks":0,"status":"SKIPPED"}, ...],
 "elapsedMillis":13}
```

实测检索 `q=分块为什么要留重叠`——`rag-notes.md#0` 命中且块内含完整的"分块策略"段落：

```
rag-notes.md#0  score=0.694  "# RAG 实践笔记\n## 分块策略\n分块是 RAG 里最容易被低估的一步。…"
embabel-overview.md#1  score=0.643
pgvector-notes.md#0  score=0.591
```

> 分块参数的效果：`embabel-overview.md`（约 500 字）被切成 **2 块**，
> 而 `rag-notes.md`（约 290 字）落在目标大小以内，因此仍是 **1 块**。
> 想看到更细的切分就把 `app.ingest.chunk-size` 调小。

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-document-ingest spring-boot:run
```

## 代码结构

- `IngestService.java` — 扫描 → 变更检测 → 分块 → 嵌入 → 入库；并负责写入示例文档
- `DocumentLoader.java` — `.md`/`.txt` 直读、`.pdf` 走 PDFBox；统一换行 + 计算哈希 + 猜标题
- `TextChunker.java` — **段落感知分块**（聚合 + 硬切 + 重叠）
- `ChunkStore.java` — `ingest_doc`（文档级哈希）+ `ingest_chunk`（块 + 向量 + HNSW）
- `IngestReport` / `RawDocument` / `Chunk` — 报告与数据模型
- `IngestController.java` — `/ingest/run`、`/ingest/demo-pdf`、`/ingest/search`、`/ingest/stats`

## 要点与坑

- **⚠️ 表名必须和别的示例区分开**：本模块第一版沿用了 `doc_chunk`，结果与
  `embabel-vector-store` 撞表——`CREATE TABLE IF NOT EXISTS` **静默跳过**（表已存在但结构不同），
  随后在旧表上建 `doc_id` 索引直接失败。多个示例共用一个 Postgres 库时，
  要么用不同表名（本模块用 `ingest_*`），要么用不同 schema/数据库。
- **PDF 的现实问题**：扫描件（图片型 PDF）抽不出文本，需要 OCR；
  双栏、表格、页眉页脚会污染文本，生产上还要清洗。
- **中文 PDF 需要嵌入字体**：本模块的演示 PDF 用 Standard14 内置字体，只能写 ASCII，
  所以内容是英文；要写中文 PDF 得 `PDType0Font.load(...)` 嵌入 TTF。
- **分块的取舍**：块越大上下文越完整但噪声与 token 成本越高；块越小召回越准但可能缺上下文。
  重叠能显著缓解"答案被切在边界"，但会让存储与嵌入成本上升（重叠部分被重复嵌入）。
- **为什么按段落而不是按长度**：固定长度会把句子、表格、代码切断，
  命中的块缺少完整语义，模型自然答不对。
- **增量是省钱的关键**：日常只改几份文档时，增量能把嵌入成本压到接近零；
  但注意"内容没变"的判断基于**抽出的文本**，所以改样式不影响、改文字才触发。
- 与 `embabel-vector-store` 的检索侧能力（元数据过滤、相似度阈值）可以叠加：
  把本模块的 `ingest_chunk` 加上 `source` 过滤与阈值，就是一套完整的生产检索链路。
