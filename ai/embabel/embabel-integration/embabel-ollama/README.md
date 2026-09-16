# embabel-ollama — 本地模型（完全离线）

## 演示内容

用**本地 Ollama** 上的模型跑 Agent——不需要任何云端 API Key，数据不出内网。

## 关键 API / 配置

| 项 | 说明 |
|---|---|
| `embabel-agent-starter-ollama` | 本地模型 Provider |
| `embabel.agent.platform.models.ollama.base-url` | Ollama 地址（未设置时回退到 `spring.ai.ollama.base-url`） |
| `embabel.agent.platform.models.ollama.nodes` | 多 Ollama 实例时的节点列表（name + baseUrl） |
| **自动发现模型** | 启动时调用 Ollama 的 `/api/tags` 拉取已安装模型，**无需手写模型清单** |
| `embabel.models.default-llm` / `default-embedding-model` | 指定默认模型（名字要与 Ollama 里一致） |

## 前置

```bash
cd ai/embabel/docker && docker compose up -d ollama
docker exec embabel-ollama ollama pull qwen2.5vl:3b        # 聊天（也可当视觉模型）
docker exec embabel-ollama ollama pull nomic-embed-text    # 嵌入
```

## 接口

```bash
curl -G --data-urlencode "message=用一句话说明什么是本地大模型" http://localhost:8925/ollama/ask
# -> {"content":"本地大模型是指在本地设备上运行的..."}

curl -G --data-urlencode "text=本地嵌入模型" http://localhost:8925/ollama/embed
# -> {"dimensions":768,"vectorLength":768,"sample":[...]}
```

启动日志（自动发现）：

```
OllamaModelsConfig - Using default Ollama instance at http://localhost:11434
OllamaModelsConfig - Ollama: Initialized 1 LLM(s) and 1 embedding(s)
ConfigurableModelProvider - Default LLM: qwen2.5vl:3b
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-ollama spring-boot:run
```

## 代码结构

- `LocalAgent.java` — 用默认（本地）模型回答
- `OllamaController.java` — `/ollama/ask`（本地聊天）与 `/ollama/embed`（本地嵌入）
- `application.yml` — 只需配 base-url 与默认模型名

## 要点

- **与 OpenAI starter 的差异**：OpenAI 需要手写 `models/openai-models.yml`（含定价、能力开关）；
  Ollama 是**自动发现**，配置量最小。
- 模型名必须与 Ollama 里一致（`docker exec embabel-ollama ollama list` 查看）；
  写错会在启动时报 `Default LLM 'x' not found`。
- 本地小模型能力弱于云端大模型，可按任务分级：简单任务本地、复杂任务云端
  （见 `embabel-multi-model` 的角色路由）。
- 同一套代码换 `base-url` 即可指向另一台 Ollama（或用 `nodes` 配多实例）。
- 想接 **Claude / Gemini / Mistral / Bedrock**，仓库里有对应 starter
  （`embabel-agent-starter-anthropic` / `-gemini` / `-google-genai` / `-mistral` / `-bedrock`），
  用法与 OpenAI starter 类似（需要各自的 Key）。
