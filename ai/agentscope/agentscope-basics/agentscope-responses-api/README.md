# agentscope-responses-api — OpenAI Responses API（agentscope-extensions-model-openai-official）

## 演示内容

AgentScope 2.0.4 新增的 Responses API 模型模块：`OpenAIResponsesChatModel` 走 OpenAI 官方 `/v1/responses` 端点（官方 SDK），支持原生结构化输出（strictJsonSchema / strictTools），对 `HarnessAgent` 透明。

> 注意：DeepSeek 等 Chat Completions 兼容端点**不支持** Responses API，需要真实 `OPENAI_API_KEY`（应用可启动，调用时才发请求）。

```bash
cd ai/agentscope/agentscope-basics/agentscope-responses-api
# 需要环境变量 OPENAI_API_KEY（OpenAI 官方）
mvn spring-boot:run
```

> 端口 9137。接口：`GET /responses/chat?message=…`（模型可用 `agentscope.responses.model` 配置，默认 gpt-4.1-mini）。
>
> 其余说明见分类 README 与组 README。
