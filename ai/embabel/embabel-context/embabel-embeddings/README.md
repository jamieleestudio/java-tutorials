# embabel-embeddings — 嵌入与语义检索

## 演示内容

用嵌入模型把文本转向量，按**余弦相似度**检索出最相关的知识片段，再让模型**只依据片段**回答——
即 RAG 的"检索 + 生成"。相比 `embabel-references` 的全量注入，这里只放最相关的几条，能显著降低 token 成本。

## 关键 API

| API | 作用 |
|---|---|
| `ai.withDefaultEmbeddingService()` | 取默认嵌入服务（由 `embabel.models.default-embedding-model` 指定） |
| `EmbeddingService.embed(String)` / `embed(List<String>)` | 文本 → `float[]` 向量 |
| `EmbeddingService.getDimensions()` | 向量维度 |
| 模型清单 `embedding_models:` | 注册嵌入模型（name / model_id / dimensions / pricing） |

## 前置条件（Docker）

DeepSeek **没有** embedding 接口，因此需要本地嵌入模型 + 代理：

```bash
cd ai/embabel/docker
cp .env.example .env            # 填 DEEPSEEK_API_KEY
docker compose up -d litellm ollama
docker compose exec ollama ollama pull nomic-embed-text
```

并且运行本模块时要把环境变量指向 LiteLLM（Embabel 环境变量优先于 yml）：

```powershell
$env:OPENAI_BASE_URL="http://localhost:4000"; $env:OPENAI_API_KEY="sk-1234"
```

```bash
export OPENAI_BASE_URL=http://localhost:4000 OPENAI_API_KEY=sk-1234
```

## 接口

```bash
curl -G --data-urlencode "message=怎么控制大知识库的 token 成本？" http://localhost:8907/embeddings/search
```

返回示例（节选）：

```json
{
  "query": "怎么控制大知识库的 token 成本？",
  "matches": [
    {"score": 0.77, "text": "大知识库应使用 embedding + 向量检索，只把最相关的片段放进提示词以控制 token 成本。"}
  ],
  "answer": "使用 embedding + 向量检索，只把最相关的片段放进提示词，以控制 token 成本。"
}
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-embeddings spring-boot:run
```

## 代码结构

- `EmbeddingsAgent.java` — 嵌入 → 余弦相似度排序 → 取 Top-3 → 生成回答
- `Corpus.java` — 示例知识库（10 条短文）
- `EmbeddingsController.java` — `GET /embeddings/search`

## 要点

- 示例每次请求都会重算语料向量（10 条，够快）。生产环境应**启动时预计算并缓存**，或接入向量数据库。
- 想接入向量库/外部检索，可实现 Embabel 的 `EagerSearch` 接口，让检索随引用一起注入。
- 归一化后的向量可直接用点积代替余弦；示例保留完整公式便于理解。
