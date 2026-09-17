# ① 基础（agentscope-basics）

## 这一章解决什么

从零开始：怎么用 AgentScope Java 建一个能跑的 Agent，怎么调模型、怎么加工具、怎么拿到结构化输出、怎么做流式。

## 模块清单

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-chat](agentscope-chat/README.md) | 9100 | 最小聊天 Agent（HarnessAgent + DeepSeek） | `GET /ai/generate` |
| [agentscope-tools](agentscope-tools/README.md) | 9101 | 工具调用（@Tool + Toolkit + @ToolParam） | `GET /tools/ask` |
| [agentscope-structured-output](agentscope-structured-output/README.md) | 9102 | 结构化输出（call + Class<?>) | `GET /structured/ask` |
| [agentscope-streaming](agentscope-streaming/README.md) | 9103 | 流式输出（Flux<Event> + StreamOptions） | `GET /stream/ask` |
| [agentscope-model-providers](agentscope-model-providers/README.md) | 9104 | 模型提供商（OpenAI/DeepSeek/Ollama） | `GET /model/ask` |

## 建议阅读顺序

1. `agentscope-chat` — 先跑通最小 Agent，理解 HarnessAgent 的构造与调用方式
2. `agentscope-tools` — 再加工具，看模型怎么自主调用
3. `agentscope-structured-output` — 让模型返回强类型对象
4. `agentscope-streaming` — 把阻塞式改成流式（SSE）
5. `agentscope-model-providers` — 换模型/多模型（OpenAI/DeepSeek/Ollama）

## 与 Embabel 的对照

| 概念 | Embabel | AgentScope |
|---|---|---|
| 最小 Agent | `@Agent` + `@Action` + `AgentInvocation` | `HarnessAgent.builder().model(...).build()` + `.call()` |
| 编排方式 | GOAP 规划器推导动作序列 | ReAct 循环（模型自主决策） |
| 工具 | `@LlmTool` 注解 | `Toolkit` + `@ToolParam` |
| 模型 | `Ai.withDefaultLlm()` | `OpenAIChatModel.builder()` |
| 返回类型 | `@AchievesGoal` + 目标类型匹配 | `RuntimeContext` + `.block()` |