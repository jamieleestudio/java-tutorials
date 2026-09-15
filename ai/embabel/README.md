# Embabel Agent Framework 示例集

用 **Java 21 + Spring Boot 3.5 + Embabel 1.0.0** 演示 Embabel 的核心能力。
共 12 个**自包含**子模块，每个模块是一个独立可运行的 Spring Boot 应用，LLM 统一接入 **DeepSeek**（OpenAI 兼容接口）。

> Embabel 是 Spring 创始人 Rod Johnson 发起的 JVM Agent 框架：用强类型领域模型 + 可复用 *Action* + GOAP 规划器，
> 让智能体围绕"目标"动态推导执行步骤，而不是写死工作流。

## 模块一览

| 模块 | 端口 | 主题 | 主要接口 |
|---|---|---|---|
| [embabel-chat](embabel-chat/README.md) | 8889 | 最小聊天 Agent（单 Action） | `GET /ai/generate` |
| [embabel-planning](embabel-planning/README.md) | 8890 | GOAP 多步规划（调研→提纲→成文） | `GET /plan/generate` |
| [embabel-tools](embabel-tools/README.md) | 8891 | 工具调用（`@LlmTool` + 函数式工具） | `GET /tools/ask` |
| [embabel-structured-output](embabel-structured-output/README.md) | 8892 | 结构化输出（强类型数据绑定） | `GET /extract` |
| [embabel-subagent](embabel-subagent/README.md) | 8893 | 子 Agent / handoff 委派 | `GET /subagent/translate` |
| [embabel-hitl](embabel-hitl/README.md) | 8894 | 人机协同（确认 / 表单，暂停与恢复） | `GET /hitl/review`、`POST /hitl/{id}/confirm` |
| [embabel-streaming](embabel-streaming/README.md) | 8895 | SSE 流式输出 | `GET /stream/generate` |
| [embabel-thinking](embabel-thinking/README.md) | 8896 | 推理过程提取（thinking） | `GET /thinking/ask` |
| [embabel-conversation](embabel-conversation/README.md) | 8897 | 多轮对话与人格 | `POST /chat/{sessionId}` |
| [embabel-guardrails](embabel-guardrails/README.md) | 8898 | 输入/输出护栏 | `GET /guardrails/ask` |
| [embabel-refinement](embabel-refinement/README.md) | 8899 | 自评迭代（Evaluator-Optimizer） | `GET /refine` |
| [embabel-planner-types](embabel-planner-types/README.md) | 8900 | GOAP / UTILITY 规划器对比 | `GET /planner/goap`、`GET /planner/utility` |

## 目录结构

```
ai/embabel/
├── pom.xml                          聚合器（packaging=pom，统一 embabel-agent.version）
├── embabel-chat/                    每个子模块：pom + src + application.yml + models/openai-models.yml
├── embabel-planning/
├── ...                              （artifactId 与目录名带 embabel- 前缀）
└── embabel-planner-types/
```

## 环境准备

- JDK 21、Maven
- 一个 DeepSeek API Key：<https://platform.deepseek.com/api_keys>

```bash
export DEEPSEEK_API_KEY=sk-xxxx
export OPENAI_BASE_URL=https://api.deepseek.com
```

> 说明：Embabel 的 OpenAI starter 同时认 `OPENAI_API_KEY`/`OPENAI_BASE_URL` 环境变量，
> 且环境变量优先级高于 yml。若你已经在用 `OPENAI_*` 指向 DeepSeek，可直接运行。

## 快速开始

```bash
cd ai/embabel
mvn -q package                     # 构建全部模块（含 embabel-chat 单测）

# 运行单个模块（端口见上表）
mvn -pl embabel-chat spring-boot:run

# 调用
curl -G --data-urlencode "message=讲个笑话" http://localhost:8889/ai/generate
```

也可以直接运行打包后的 jar：

```bash
java -jar embabel-chat/target/embabel-chat-1.0.jar
```

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
`src/main/resources/models/openai-models.yml` 放了同名文件，**覆盖** jar 内置的清单
（classpath 同路径下应用资源优先），从而把 DeepSeek 模型注册进去。

两个 DeepSeek 特有点：

1. **结构化输出**：DeepSeek 只支持 `response_format={"type":"json_object"}`，不支持 OpenAI 的 `json_schema`。
   因此清单里设置 `native_support_defaults.structured_output.supported: false`，Embabel 会自动回退为提示词方案。
2. **thinking 提取**：DeepSeek 的推理在独立的 `reasoning_content` 字段，而 Embabel 的提取器识别的是内容中的
   `<think>...</think>` 标签。要用 thinking 提取，需在提示词中显式要求用标签包裹推理。

## 常见问题

| 现象 | 原因 / 解决 |
|---|---|
| 启动报 `Default LLM 'xxx' not found` | `embabel.models.default-llm` 与 `models/openai-models.yml` 里的 `name` 不一致 |
| 调用返回 401 / 无响应 | 未设置 `DEEPSEEK_API_KEY`（或 `OPENAI_API_KEY`），或 base-url 不对 |
| 护栏违规的请求非常慢 | Embabel 默认动作重试 5 次、数据绑定重试 10 次；见 embabel-guardrails 的快速失败配置 |
| HITL 恢复后状态是 `STUCK` | 等待对象的 payload 类型必须等于该 Action 的返回类型（表单要先收对象、再由后续 Action 消费） |
| 模型自称是别的模型 | 大模型自我认知不可靠，属正常现象；接口调用确实走的是 `api.deepseek.com` |

## 构建与测试

```bash
cd ai/embabel
mvn package        # 全部模块 + embabel-chat 的 Mockito 单测
mvn -pl embabel-chat test
```
