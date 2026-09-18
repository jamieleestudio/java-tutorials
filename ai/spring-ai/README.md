# Spring AI 教程

用 **Java 21 + Spring Boot 4.0.3 + Spring AI 2.0.0** 演示 Spring AI 的核心能力与编排模式。

> Spring AI 是 Spring 官方的 AI 应用框架。2.0 相比 1.x 有重大变化：
> **工具调用下沉到 ToolCallingAdvisor**、**RAG 改为三段式管道**（Retriever + Augmenter + Transformer）、
> **Jackson 3.x**（`tools.jackson.databind`）、**ChatOptions.Builder** 替代直接构造。
> 模型接入 DeepSeek（OpenAI 兼容接口，`base-url=https://api.deepseek.com`）。

## 与 Embabel / AgentScope 的定位差异

| 维度 | Spring AI | Embabel | AgentScope |
|---|---|---|---|
| 定位 | 通用 AI 集成框架 | 类型化建模 + GOAP 规划 | ReAct 循环 + Middleware |
| 核心抽象 | `ChatClient` + Advisor | `@Agent` + `@Action` | `HarnessAgent` + `MiddlewareBase` |
| 工具 | `@Tool` + FunctionToolCallback | `@LlmTool` | `@Tool` + `Toolkit` |
| 记忆 | `ChatMemory` + Advisor | Process 状态 | `AgentStateStore` + `MemoryConfig` |
| RAG | VectorStore + Retriever/Augmenter | 无内置 | Knowledge + KnowledgeRetrievalTools |
| 中间件 | Advisor（before/after） | — | MiddlewareBase（5 钩子） |

## 分类与模块（27 模块 / 7 分类，全部编译通过）

### ① [基础（spring-ai-basics）](spring-ai-basics/README.md) — 端口 8000-8003

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [spring-ai-chat](spring-ai-basics/spring-ai-chat/README.md) | 8000 | 基础聊天（ChatClient + ChatModel） | `GET /ai/chat` |
| [spring-ai-streaming](spring-ai-basics/spring-ai-streaming/README.md) | 8001 | 流式输出（Flux + SSE） | `GET /ai/streaming` |
| [spring-ai-structured-output](spring-ai-basics/spring-ai-structured-output/README.md) | 8002 | 结构化输出（entity + BeanOutputConverter） | `GET /ai/structured` |
| [spring-ai-prompt-templates](spring-ai-basics/spring-ai-prompt-templates/README.md) | 8003 | 提示模板（PromptTemplate 参数化） | `GET /ai/prompt` |

### ② [工具与函数调用（spring-ai-tools）](spring-ai-tools/README.md) — 端口 8004-8006

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [spring-ai-function-calling](spring-ai-tools/spring-ai-function-calling/README.md) | 8004 | 函数调用（@Tool + tools()） | `GET /ai/function` |
| [spring-ai-tool-context](spring-ai-tools/spring-ai-tool-context/README.md) | 8005 | 工具上下文（ToolContext 动态传参） | `GET /ai/tool-context` |
| [spring-ai-mcp-client](spring-ai-tools/spring-ai-mcp-client/README.md) | 8006 | MCP 客户端（接入 MCP server） | `GET /ai/mcp` |

### ③ [Advisor 链（spring-ai-advisors）](spring-ai-advisors/README.md) — 端口 8007-8009

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [spring-ai-advisor-basics](spring-ai-advisors/spring-ai-advisor-basics/README.md) | 8007 | Advisor 基础（SimpleLoggerAdvisor + 自定义 BaseAdvisor） | `GET /ai/advisor` |
| [spring-ai-safeguard](spring-ai-advisors/spring-ai-safeguard/README.md) | 8008 | 安全护栏（SafeGuardAdvisor 敏感词拦截） | `GET /ai/safeguard` |
| [spring-ai-chat-memory-advisor](spring-ai-advisors/spring-ai-chat-memory-advisor/README.md) | 8009 | 对话记忆（MessageChatMemoryAdvisor） | `GET /ai/memory` |

### ④ [RAG 与知识库（spring-ai-rag）](spring-ai-rag/README.md) — 端口 8010-8012

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [spring-ai-embedding](spring-ai-rag/spring-ai-embedding/README.md) | 8010 | 向量嵌入（EmbeddingModel） | `GET /ai/embedding` |
| [spring-ai-vector-store](spring-ai-rag/spring-ai-vector-store/README.md) | 8011 | 向量存储（SimpleVectorStore 增删查） | `GET /ai/vector/search` |
| [spring-ai-rag-basics](spring-ai-rag/spring-ai-rag-basics/README.md) | 8012 | RAG 检索增强（Retriever + Augmenter 三段式） | `GET /ai/rag` |

### ⑤ [多模态与 ETL（spring-ai-multimodal-etl）](spring-ai-multimodal-etl/README.md) — 端口 8013-8015

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [spring-ai-multimodal](spring-ai-multimodal-etl/spring-ai-multimodal/README.md) | 8013 | 多模态（图片输入 + Media） | `GET /ai/multimodal` |
| [spring-ai-etl](spring-ai-multimodal-etl/spring-ai-etl/README.md) | 8014 | ETL 管道（Reader + Splitter + VectorStore） | `GET /ai/etl` |
| [spring-ai-observability](spring-ai-multimodal-etl/spring-ai-observability/README.md) | 8015 | 可观测性（Micrometer + 日志） | `GET /ai/observability` |

### ⑥ [编排模式 ★（spring-ai-patterns）](spring-ai-patterns/README.md) — 端口 8016-8025

| 模块 | 端口 | 模式 | 接口 |
|---|---|---|---|
| [spring-ai-prompt-chaining](spring-ai-patterns/spring-ai-prompt-chaining/README.md) | 8016 | Prompt Chaining + 关卡 | `GET /ai/prompt-chain` |
| [spring-ai-routing](spring-ai-patterns/spring-ai-routing/README.md) | 8017 | 路由分类 | `GET /ai/routing` |
| [spring-ai-parallelization](spring-ai-patterns/spring-ai-parallelization/README.md) | 8018 | 并行（Flux.merge 多 ChatClient） | `GET /ai/parallel` |
| [spring-ai-orchestrator-workers](spring-ai-patterns/spring-ai-orchestrator-workers/README.md) | 8019 | 编排者-工人（@Tool 委派） | `GET /ai/orchestrator` |
| [spring-ai-refinement](spring-ai-patterns/spring-ai-refinement/README.md) | 8020 | 自评迭代 | `GET /ai/refinement` |
| [spring-ai-multi-agent-debate](spring-ai-patterns/spring-ai-multi-agent-debate/README.md) | 8021 | 多 Agent 辩论 | `GET /ai/debate` |
| [spring-ai-tree-of-thoughts](spring-ai-patterns/spring-ai-tree-of-thoughts/README.md) | 8022 | 思维树（多 temperature） | `GET /ai/tot` |
| [spring-ai-self-reflection](spring-ai-patterns/spring-ai-self-reflection/README.md) | 8023 | 自省 + 渐进式工具 | `GET /ai/self-reflection` |
| [spring-ai-state-machine](spring-ai-patterns/spring-ai-state-machine/README.md) | 8024 | 状态机（Advisor 阶段收敛） | `GET /ai/state-machine` |
| [spring-ai-capstone-patterns](spring-ai-patterns/spring-ai-capstone-patterns/README.md) | 8025 | 模式综合 | `GET /ai/capstone` |

### ⑦ [综合（spring-ai-capstone）](spring-ai-capstone/README.md) — 端口 8026

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [spring-ai-capstone-app](spring-ai-capstone/spring-ai-capstone-app/README.md) | 8026 | 端到端客服 Agent（RAG+记忆+工具+安全） | `GET /ai/capstone` |

## 快速开始

```bash
# 编译所有模块（需本地 m2 有依赖；离线模式）
cd ai/spring-ai
mvn package -DskipTests

# 运行单个模块（需要 OPENAI_API_KEY 环境变量 = DeepSeek key）
cd spring-ai-basics/spring-ai-chat
mvn spring-boot:run
```

## 关键 API（2.0）

| 能力 | 核心类 | 用法 |
|---|---|---|
| 聊天 | `ChatClient` | `ChatClient.create(model).prompt(msg).call().content()` |
| 流式 | `ChatModel.stream` | `chatModel.stream(message)` → `Flux<String>` |
| 结构化 | `entity()` | `chatClient.prompt().call().entity(MyClass.class)` |
| 工具 | `@Tool` + `ToolCallingAdvisor` | `chatClient.prompt().tools(myBean).call()` |
| 工具上下文 | `ToolContext` | 工具方法加 `ToolContext` 参数注入运行时数据 |
| Advisor | `BaseAdvisor.before/after` | `.defaultAdvisors(new MyAdvisor())` |
| 安全 | `SafeGuardAdvisor` | `.sensitiveWords(List).failureResponse(msg)` |
| 记忆 | `MessageChatMemoryAdvisor` + `MessageWindowChatMemory` | `.defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())` |
| 嵌入 | `EmbeddingModel` | `embeddingModel.embed(text)` |
| 向量库 | `VectorStore` + `SimpleVectorStore` | `SimpleVectorStore.builder(embeddingModel).build()` |
| RAG | `VectorStoreDocumentRetriever` + `ContextualQueryAugmenter` | 三段式：检索 → 增强 → 生成 |
| 多模态 | `Media` | `user(u -> u.media(mimeType, resource))` |
| 提示模板 | `PromptTemplate` | `new PromptTemplate(tpl).render(params)` |

## 技术栈

- Java 21
- Spring Boot 4.0.3
- Spring AI 2.0.0（BOM）
- DeepSeek（OpenAI 兼容接口）
- Reactor（Flux/Mono 响应式）
