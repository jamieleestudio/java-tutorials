# 外部组件（Docker）

`embabel-embeddings`、`embabel-multimodal` 需要本地模型能力（DeepSeek 没有 embedding / 视觉接口），
`embabel-mcp` 需要本机能执行 `docker`。这里提供统一的 compose 配置。

## 服务与端口

| 服务 | 端口 | 用途 |
|---|---|---|
| LiteLLM | 4000 | 统一 OpenAI 兼容入口：聊天转发 DeepSeek，嵌入/视觉转发 Ollama |
| Ollama | 11434 | 本地模型：`nomic-embed-text`（嵌入，768 维）、`qwen2.5vl:3b`（视觉） |
| Postgres | 5433 | `embabel-persistence` 的上下文存储（`embabel_context` 表自动创建）<br>`embabel-vector-store` 的向量库（`pgvector/pgvector:pg16` 镜像，`doc_chunk` 表 + HNSW 索引） |

> 端口刻意避开常见的 80/443/8080/8081/5003（本机已有 nginx / dependency-track / Dify 在跑）。

## 为什么需要 LiteLLM

Embabel 的 OpenAI starter 用**同一个 base-url** 访问该 provider 下的所有模型（聊天 + 嵌入）。
DeepSeek 只有聊天，本地 Ollama 才有嵌入/视觉，于是用 LiteLLM 把两者统一到一个端点：
应用只需指向 `http://localhost:4000`。

## 启动

```bash
cd ai/embabel/docker
cp .env.example .env         # 填入 DEEPSEEK_API_KEY（Windows: copy .env.example .env）
docker compose up -d         # 启动 litellm + ollama + postgres
docker compose exec ollama ollama pull nomic-embed-text
docker compose exec ollama ollama pull qwen2.5vl:3b
```

镜像说明：`litellm` 由本目录的 `Dockerfile` **本地构建**（官方镜像补装 Pillow，
否则视觉请求会报 `ollama image conversion failed please run pip install Pillow`）。

## 项目名隔离（重要）

compose 文件里显式写了 `name: embabel`。如果不指定项目名，Docker 会用目录名 `docker` 作为项目名，
与**同样位于名为 `docker` 目录的其他 compose 项目**（例如 Dify）冲突，
`docker compose` 会把对方的容器当成 orphan 容器——一旦执行 `--remove-orphans` 就会误删。

## 应用侧环境变量（重要）

Embabel 的 OpenAI starter **环境变量优先于 yml**（`OPENAI_BASE_URL` / `OPENAI_API_KEY`）。
所以运行需要走 LiteLLM 的模块（embeddings / multimodal）时，必须把这两个变量指向 LiteLLM：

```powershell
$env:OPENAI_BASE_URL="http://localhost:4000"; $env:OPENAI_API_KEY="sk-1234"
```

```bash
export OPENAI_BASE_URL=http://localhost:4000 OPENAI_API_KEY=sk-1234
```

（`sk-1234` 即 `.env` 里的 `LITELLM_MASTER_KEY`；真正的 DeepSeek Key 只存在于 `.env`，由 LiteLLM 使用。）

## 常用命令

```bash
docker compose ps                     # 状态
docker compose logs -f litellm        # 看 LiteLLM 日志
docker compose down                   # 停止（保留数据卷）
docker compose down -v                # 停止并删除数据卷（会清掉已下载的模型）

# 验证
curl http://localhost:4000/health/liveliness
curl -H "Authorization: Bearer sk-1234" -H "Content-Type: application/json" \
  -d '{"model":"nomic-embed-text","input":["hello"]}' http://localhost:4000/v1/embeddings
```

## 故障排查

| 现象 | 解决 |
|---|---|
| `ollama image conversion failed ... Pillow` | 用本目录 `Dockerfile` 重建：`docker compose up -d --build litellm` |
| `Found orphan containers (...)` | 确认 compose 里有 `name: embabel`；不要对别人的项目用 `--remove-orphans` |
| 调用报 401/404 且日志显示 DeepSeek 的模型名 | 环境变量 `OPENAI_BASE_URL` 覆盖了 yml，按上文指向 LiteLLM |
| 首次视觉请求很慢 | Ollama 需要把模型加载进内存，首次通常 30–60s |
