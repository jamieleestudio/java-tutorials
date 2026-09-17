# embabel-memory — 跨会话长期记忆

## 演示内容

**上次说过的事，这次还知道。** 这条链路之前是空白：

| 已有能力 | 解决什么 | 缺什么 |
|---|---|---|
| `embabel-conversation` | 同一会话内的多轮记忆 | 会话结束就没了 |
| `embabel-persistence` | 上下文落库（`ContextRepository`） | **不会在新会话里主动召回** |
| `embabel-vector-store` / `document-ingest` | 公共知识的检索 | 检索的不是"这个用户"的历史 |
| **`embabel-memory`（本模块）** | **按用户召回历史偏好/事实** | — |

流程：`问题向量化 → 按 userId 召回 top-k → 拼进提示词 → 回答`

## 实测：两个"会话"、两个用户

**会话 1**（写入记忆）：

```bash
curl -X POST --data-urlencode "userId=alice" \
     --data-urlencode "text=我偏好用 Kotlin 写 Agent，不喜欢 Python" \
     --data-urlencode "kind=preference" http://localhost:8945/memory/remember
curl -X POST --data-urlencode "userId=alice" \
     --data-urlencode "text=我们的服务部署在 Kubernetes 上，集群在阿里云" \
     --data-urlencode "kind=fact" http://localhost:8945/memory/remember
curl -X POST --data-urlencode "userId=bob" --data-urlencode "text=我偏好用 Java" \
     http://localhost:8945/memory/remember
```

**会话 2**（全新会话，无任何会话历史）——`GET /memory/answer?userId=alice&question=给我写个最小 Agent 示例`：

- 召回：`我偏好用 Kotlin 写 Agent…`（0.687）、`我们的服务部署在 Kubernetes 上…`（0.532）
- 回答：给出 **Kotlin** 示例，并在部署建议里提到 **K8s / Secret 注入 / Pod**

**对照**（`userId=bob`，同样的问题）：

- 召回：只有 `我偏好用 Java`（**看不到 alice 的记忆**）
- 回答：给出 **Java** 示例

这两条对照说明：**记忆确实按用户隔离**，且跨会话生效。

原始召回（可看相似度）：

```json
[{"kind":"preference","text":"我偏好用 Kotlin 写 Agent，不喜欢 Python","score":0.6869},
 {"kind":"fact","text":"我们的服务部署在 Kubernetes 上，集群在阿里云","score":0.5321}]
```

## 关键 API / 做法

| 项 | 说明 |
|---|---|
| `OperationContext.user()` | 动作里拿当前用户 → 作为 `user_id`（身份由调用方用 `ProcessOptions.withIdentities(...)` 传入） |
| `EmbeddingService.embed(text)` | 写入与召回都要先向量化 |
| `WHERE user_id = ? ORDER BY embedding <=> ?::vector` | **按用户过滤 + 向量排序**（少了 WHERE 就会串号） |
| `kind`（preference / fact） | 区分偏好与事实，便于在提示词里给不同权重 |
| `DELETE FROM user_memory WHERE user_id = ?` | "被遗忘权"的最小实现 |
| 表名 `user_memory` | 与 `embabel-vector-store`（`doc_chunk`）、`embabel-document-ingest`（`ingest_*`）区分开 |

## 前置条件

```bash
cd ai/embabel/docker
docker compose up -d postgres litellm ollama
docker compose exec ollama ollama pull nomic-embed-text
$env:OPENAI_BASE_URL="http://localhost:4000"; $env:OPENAI_API_KEY="sk-1234"
```

## 接口

```bash
curl -X POST --data-urlencode "userId=alice" --data-urlencode "text=..." \
     --data-urlencode "kind=preference" http://localhost:8945/memory/remember
curl -G --data-urlencode "userId=alice" --data-urlencode "query=..." http://localhost:8945/memory/recall
curl -G --data-urlencode "userId=alice" --data-urlencode "question=..." http://localhost:8945/memory/answer
curl -X POST --data-urlencode "userId=alice" http://localhost:8945/memory/forget
curl -G --data-urlencode "userId=alice" http://localhost:8945/memory/stats
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-memory spring-boot:run
```

## 代码结构

- `MemoryStore.java` — `user_memory` 表（含 `user_id`/`kind`）+ 写入 / 召回 / 遗忘 / 计数
- `MemoryAgent.java` — 召回 → 拼提示词 → 作答（身份来自 `OperationContext.user()`）
- `MemoryController.java` — 5 个端点
- `MemoryItem` / `MemoryAnswer` — 记忆条目与目标类型

## 要点

- **长期记忆 ≠ 会话历史 ≠ RAG**：三者检索的"范围"不同（本会话 / 落库不召回 / 公共知识 vs
  **该用户的历史**）。别用其中一个替代另一个。
- **必须按用户过滤**：这是长期记忆最容易出的安全事故——忘了 `WHERE user_id`，
  A 用户的偏好会出现在 B 用户的回答里。本模块的对照实验就是为了证明这条 WHERE 真的在起作用。
- **写入要克制**：不是每句话都值得记。生产上通常只记"明确表达偏好/事实"的句子，
  并做去重（同一件事多次说 → 更新而不是新增）与时效衰减。
- **`kind` 让提示词更聪明**：偏好（"我不喜欢 Python"）比事实（"集群在阿里云"）更稳定，
  可以在拼提示词时给偏好更高权重；事实则应该带上时间戳以便判断是否过期。
- **合规**：长期记忆属于个人数据，需要提供查询（`/recall`）与删除（`/forget`）能力，
  并考虑保留期限。本模块的 `forget` 就是这条要求的最小实现。
- **与 `embabel-identity` 的组合**：身份用 `Identities`/`User`（那个模块教的），
  记忆用 `user_id` 过滤（本模块教的）——**身份是记忆隔离的前提**。
- 进一步方向：记忆去重与合并、时效衰减、按 `kind` 分层召回、
  以及"从对话中自动抽取值得记的句子"（这一步通常也用一次 LLM 调用）。
