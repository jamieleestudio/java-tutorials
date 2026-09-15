# Embabel Agent Framework 示例集

用 **Java 21 + Spring Boot 3.5 + Embabel 1.0.0** 演示 Embabel 的核心能力。
共 21 个**自包含**子模块，按能力分为 7 类；每个模块是一个独立可运行的 Spring Boot 应用，
默认 LLM 接入 **DeepSeek**（OpenAI 兼容接口），需要嵌入/视觉时经 LiteLLM 接入本地 Ollama。

> Embabel 是 Spring 创始人 Rod Johnson 发起的 JVM Agent 框架：用强类型领域模型 + 可复用 *Action* + GOAP 规划器，
> 让智能体围绕"目标"动态推导执行步骤，而不是写死工作流。

## 分类与模块

### ① 基础（embabel-basics）

| 模块 | 端口 | 主题 | 主要接口 |
|---|---|---|---|
| [embabel-chat](embabel-basics/embabel-chat/README.md) | 8889 | 最小聊天 Agent（单 Action） | `GET /ai/generate` |
| [embabel-planning](embabel-basics/embabel-planning/README.md) | 8890 | GOAP 多步规划（调研→提纲→成文） | `GET /plan/generate` |
| [embabel-tools](embabel-basics/embabel-tools/README.md) | 8891 | 工具调用（`@LlmTool` + 函数式工具） | `GET /tools/ask` |
| [embabel-structured-output](embabel-basics/embabel-structured-output/README.md) | 8892 | 结构化输出（强类型数据绑定） | `GET /extract` |

### ② 上下文工程（embabel-context）

| 模块 | 端口 | 主题 | 主要接口 |
|---|---|---|---|
| [embabel-references](embabel-context/embabel-references/README.md) | 8901 | 引用加载 / 轻量 RAG（`LlmReference`） | `GET /references/ask` |
| [embabel-file-tools](embabel-context/embabel-file-tools/README.md) | 8902 | 沙箱文件工具（`FileTools`） | `GET /files/ask` |
| [embabel-embeddings](embabel-context/embabel-embeddings/README.md) | 8907 | 嵌入与语义检索（需 Docker） | `GET /embeddings/search` |

### ③ 交互与协作（embabel-interaction）

| 模块 | 端口 | 主题 | 主要接口 |
|---|---|---|---|
| [embabel-subagent](embabel-interaction/embabel-subagent/README.md) | 8893 | 子 Agent / handoff 委派 | `GET /subagent/translate` |
| [embabel-hitl](embabel-interaction/embabel-hitl/README.md) | 8894 | 人机协同（确认 / 表单，暂停与恢复） | `GET /hitl/review`、`POST /hitl/{id}/confirm` |
| [embabel-streaming](embabel-interaction/embabel-streaming/README.md) | 8895 | SSE 流式输出 | `GET /stream/generate` |
| [embabel-conversation](embabel-interaction/embabel-conversation/README.md) | 8897 | 多轮对话与人格 | `POST /chat/{sessionId}` |
| [embabel-multimodal](embabel-interaction/embabel-multimodal/README.md) | 8908 | 图像理解（需 Docker） | `GET /multimodal/describe` |

### ④ 推理与规划（embabel-reasoning）

| 模块 | 端口 | 主题 | 主要接口 |
|---|---|---|---|
| [embabel-thinking](embabel-reasoning/embabel-thinking/README.md) | 8896 | 推理过程提取（thinking） | `GET /thinking/ask` |
| [embabel-refinement](embabel-reasoning/embabel-refinement/README.md) | 8899 | 自评迭代（Evaluator-Optimizer） | `GET /refine` |
| [embabel-planner-types](embabel-reasoning/embabel-planner-types/README.md) | 8900 | GOAP / UTILITY 规划器对比 | `GET /planner/goap`、`GET /planner/utility` |
| [embabel-multi-model](embabel-reasoning/embabel-multi-model/README.md) | 8905 | 角色→模型映射与回退 | `GET /multi-model/ask`、`/multi-model/fallback` |
| [embabel-workflows](embabel-reasoning/embabel-workflows/README.md) | 8906 | 工作流原语（Kotlin：ScatterGather / Consensus） | `GET /workflows/scatter-gather`、`/workflows/consensus` |

### ⑤ 质量与安全（embabel-safety）

| 模块 | 端口 | 主题 | 主要接口 |
|---|---|---|---|
| [embabel-guardrails](embabel-safety/embabel-guardrails/README.md) | 8898 | 输入/输出护栏 | `GET /guardrails/ask` |

### ⑥ 工程化（embabel-ops）

| 模块 | 端口 | 主题 | 主要接口 |
|---|---|---|---|
| [embabel-observability](embabel-ops/embabel-observability/README.md) | 8903 | 事件监听 + 成本/Token 统计 | `GET /observability/run` |
| [embabel-testing](embabel-ops/embabel-testing/README.md) | 8904 | 无需 API Key 的确定性测试 | `mvn -pl :embabel-testing test` |

### ⑦ 外部集成（embabel-integration）

| 模块 | 端口 | 主题 | 主要接口 |
|---|---|---|---|
| [embabel-mcp](embabel-integration/embabel-mcp/README.md) | 8909 | MCP 工具（filesystem server） | `GET /mcp/ask` |

## 目录结构

```
ai/embabel/
├── pom.xml                        总聚合（packaging=pom，parent=spring-boot-starter-parent，管版本）
├── README.md
├── docker/                        外部组件（LiteLLM + Ollama + Postgres），见 docker/README.md
├── embabel-basics/                ① 基础
├── embabel-context/               ② 上下文工程（references / file-tools / embeddings）
├── embabel-interaction/           ③ 交互与协作（subagent / hitl / streaming / conversation / multimodal）
├── embabel-reasoning/             ④ 推理与规划（thinking / refinement / planner-types / multi-model / workflows）
├── embabel-safety/                ⑤ 质量与安全（guardrails）
├── embabel-ops/                   ⑥ 工程化（observability / testing）
└── embabel-integration/           ⑦ 外部集成（mcp）
```

每个叶子模块的 `parent` 指向所属分类聚合器，分类聚合器的 `parent` 指向 `ai/embabel`，
因此 `embabel-agent.version`、Java 版本、依赖版本统一在顶层维护。
**artifactId 与目录名一致**（都带 `embabel-` 前缀），所以 `mvn -pl :embabel-chat` 这类命令不受分类影响。

## 环境准备

- JDK 21、Maven
- 一个 DeepSeek API Key：<https://platform.deepseek.com/api_keys>
- （仅部分模块）Docker：`embabel-embeddings` / `embabel-multimodal` 需要嵌入/视觉模型，
  `embabel-mcp` 需要 docker 拉起 MCP server

```bash
export DEEPSEEK_API_KEY=sk-xxxx
export OPENAI_BASE_URL=https://api.deepseek.com
```

> Embabel 的 OpenAI starter 同时认 `OPENAI_API_KEY`/`OPENAI_BASE_URL`，**且环境变量优先于 yml**。
> 若你已用 `OPENAI_*` 指向 DeepSeek，可直接运行大多数模块。
> `embabel-testing` 不需要任何 Key。

## 快速开始

```bash
cd ai/embabel
mvn -q package                     # 构建全部模块（含单测）

# 运行单个模块（-pl 用 :artifactId，不受分类层级影响）
mvn -pl :embabel-chat spring-boot:run

# 调用
curl -G --data-urlencode "message=讲个笑话" http://localhost:8889/ai/generate
```

也可以直接运行打包后的 jar：

```bash
java -jar embabel-basics/embabel-chat/target/embabel-chat-1.0.jar
```

## 外部组件（Docker）

需要嵌入/视觉/Postgres 的模块，先启动 `docker/` 下的组件（端口 4000 / 11434 / 5433，已避开常用占用）：

```bash
cd ai/embabel/docker
cp .env.example .env               # 填 DEEPSEEK_API_KEY
docker compose up -d
docker compose exec ollama ollama pull nomic-embed-text
docker compose exec ollama ollama pull qwen2.5vl:3b
```

LiteLLM 把 **DeepSeek 聊天 + Ollama 嵌入/视觉**统一到一个 OpenAI 兼容端点（因为 Embabel 一个 base-url 管所有模型）。
细节见 [docker/README.md](docker/README.md)。

## DeepSeek 接入说明（重要）

Embabel 的 OpenAI 支持**不读** `spring.ai.openai.*`，而是用它自己的属性：

```yaml
embabel:
  models:
    default-llm: deepseek-flash        # 必须等于 models/openai-models.yml 中某个模型的 name
  agent:
    platform:
      models:
        openai:
          base-url: ${OPENAI_BASE_URL:placeholder}   # https://api.deepseek.com
          completions: /chat/completions
          api-key: ${DEEPSEEK_API_KEY:placeholder}
```

模型清单从 classpath `models/openai-models.yml` 加载。每个模块都在
`src/main/resources/models/openai-models.yml` 放了同名文件，**覆盖** jar 内置的清单。

两个 DeepSeek 特有点：

1. **结构化输出**：DeepSeek 只支持 `response_format={"type":"json_object"}`，因此清单里设置
   `native_support_defaults.structured_output.supported: false`，Embabel 自动回退为提示词方案。
2. **thinking 提取**：Embabel 的提取器识别内容中的 `<think>...</think>` 标签（DeepSeek 的原生推理在
   `reasoning_content` 字段），需在提示词里显式要求用标签包裹推理。

## 常见问题

| 现象 | 原因 / 解决 |
|---|---|
| 启动报 `Default LLM 'xxx' not found` | `embabel.models.default-llm` 与清单里的 `name` 不一致 |
| 调用返回 401 / 无响应 | 未设置 `DEEPSEEK_API_KEY`（或 `OPENAI_API_KEY`），或 base-url 不对 |
| 嵌入/视觉模块报 DeepSeek 的模型名错误 | **环境变量优先于 yml**：把 `OPENAI_BASE_URL`/`OPENAI_API_KEY` 指向 LiteLLM（`http://localhost:4000` / `sk-1234`） |
| 视觉请求报 `ollama image conversion failed ... Pillow` | LiteLLM 镜像需补 Pillow：`docker compose up -d --build litellm` |
| `Found orphan containers (...)` | compose 已设 `name: embabel`；不要对别人的项目用 `--remove-orphans` |
| 模型输出 `<tool_calls>` 之类的 XML | 提示词里出现 `Tool prefix` 但实际没有工具；见 embabel-references |
| 护栏违规的请求非常慢 | 默认动作重试 5 次、数据绑定重试 10 次；见 embabel-guardrails 的快速失败配置 |
| HITL 恢复后状态是 `STUCK` | 等待对象的 payload 类型必须等于该 Action 的返回类型 |
| Kotlin 模块编译报 metadata 版本不兼容 | Embabel 的 Kotlin metadata 是 2.1.0，需把 `kotlin.version` 覆盖为 2.1.0（见 embabel-workflows） |

## 构建与测试

```bash
cd ai/embabel
mvn package                          # 全部模块 + 各模块单测
mvn -pl :embabel-testing test        # 只跑测试示例模块（无需 API Key）
```
