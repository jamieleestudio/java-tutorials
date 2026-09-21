# LangChain4j 教程

用 **Java 21 + Spring Boot 4.0.3 + LangChain4j 1.20.0** 演示 LangChain4j 的核心能力：
ChatModel、AiServices 声明式接口、工具、记忆、RAG（本地 BGE-zh 嵌入）、Guardrails、
Agentic 模式框架、MCP 与编排模式。

> LangChain4j 是 Java 生态最早的大模型应用框架之一，招牌是 **AiServices 声明式接口**
> （注解即提示词，方法即调用）与 **langchain4j-agentic** 官方模式框架
> （AgenticScope 共享状态 + sequence/loop/parallel/conditional/supervisor 六种模式 +
> 错误恢复 + 跨 Agent 补偿 + AgentMonitor HTML 报告）。
> RAG 嵌入使用 `langchain4j-embeddings-bge-small-zh` 本地 CPU 推理，零 API key。

## 运行方式

LangChain4j 组通过根 package.json 的 Maven 聚合 pom 组织（10 分类 / 28 模块）：

```bash
# 【等价 mvn package】编译全组
cd ai/langchain4j
mvn compile

# 单模块运行（需要环境变量 DEEPSEEK_API_KEY = DeepSeek key）
cd ai/langchain4j/langchain4j-basics/langchain4j-chat
mvn spring-boot:run
```

## 与其他组的定位差异

| 维度 | LangChain4j | Spring AI / SAA | Embabel | AgentScope |
|---|---|---|---|---|
| 声明式 | AiServices 接口 + 注解模板 | ChatClient 流式 API | @Agent + GOAP | HarnessAgent |
| Agentic 模式 | **官方 agentic 模块**（六种模式一等公民） | Graph 手写编排 | 规划器推导 | Middleware 约束 |
| 嵌入 | **本地 ONNX（BGE-zh，零 key）** | API/本地均有 | 本地/API | API |
| 护栏 | Guardrails（输入/输出） | — | 内建 | Permission |

## 分类与模块（28 模块 / 10 分类，全部编译通过，核心链路已运行验证）

### ① [basics](langchain4j-basics/README.md) — 端口 8700-8703

| 模块 | 端口 | 主题 |
|---|---|---|
| [langchain4j-chat](langchain4j-basics/langchain4j-chat/README.md) | 8700 | ChatModel 低层 API（便捷重载 + ChatRequest） |
| [langchain4j-ai-services](langchain4j-basics/langchain4j-ai-services/README.md) | 8701 | AiServices 声明式接口 + 模板注解 |
| [langchain4j-streaming](langchain4j-basics/langchain4j-streaming/README.md) | 8702 | 流式输出（TokenStream → SSE） |
| [langchain4j-structured-output](langchain4j-basics/langchain4j-structured-output/README.md) | 8703 | 结构化输出（返回 POJO 自动解析） |

### ② [tools](langchain4j-tools/README.md) — 端口 8704-8705

| 模块 | 端口 | 主题 |
|---|---|---|
| [langchain4j-tools-basics](langchain4j-tools/langchain4j-tools-basics/README.md) | 8704 | @Tool 注解工具（AiServices.tools） |
| [langchain4j-tools-advanced](langchain4j-tools/langchain4j-tools-advanced/README.md) | 8705 | 编程式工具（ToolSpecification + ToolExecutor） |

### ③ [memory](langchain4j-memory/README.md) — 端口 8706-8708

| 模块 | 端口 | 主题 |
|---|---|---|
| [langchain4j-chat-memory](langchain4j-memory/langchain4j-chat-memory/README.md) | 8706 | MessageWindowChatMemory 多轮对话 |
| [langchain4j-persistent-memory](langchain4j-memory/langchain4j-persistent-memory/README.md) | 8707 | 自定义 ChatMemoryStore 文件持久化 |
| [langchain4j-multi-user](langchain4j-memory/langchain4j-multi-user/README.md) | 8708 | ChatMemoryProvider 多用户隔离 |

### ④ [RAG ★](langchain4j-rag/README.md) — 端口 8709-8712

| 模块 | 端口 | 主题 |
|---|---|---|
| [langchain4j-rag-embeddings](langchain4j-rag/langchain4j-rag-embeddings/README.md) | 8709 | BGE-zh 本地嵌入 + InMemoryEmbeddingStore |
| [langchain4j-rag-etl](langchain4j-rag/langchain4j-rag-etl/README.md) | 8710 | ETL 管道（Parser + Splitter + Embed） |
| [langchain4j-rag-basics](langchain4j-rag/langchain4j-rag-basics/README.md) | 8711 | ContentRetriever + AiServices RAG 问答 |
| [langchain4j-rag-advanced](langchain4j-rag/langchain4j-rag-advanced/README.md) | 8712 | DefaultRetrievalAugmentor 双源 QueryRouter |

### ⑤ [guardrails](langchain4j-guardrails/README.md) — 端口 8713-8714

| 模块 | 端口 | 主题 |
|---|---|---|
| [langchain4j-guardrails](langchain4j-guardrails/langchain4j-guardrails-basics/README.md) | 8713 | InputGuardrail 手机号打码 |
| [langchain4j-classification](langchain4j-guardrails/langchain4j-classification/README.md) | 8714 | 枚举返回文本分类 |

### ⑥ [spring-boot](langchain4j-spring-boot/README.md) — 端口 8715

| 模块 | 端口 | 主题 |
|---|---|---|
| [langchain4j-spring-boot-starter](langchain4j-spring-boot/langchain4j-spring-boot-starter/README.md) | 8715 | Spring Boot 集成（Boot 4 兼容性验证点） |

### ⑦ [agentic ★](langchain4j-agentic/README.md) — 端口 8716-8721

| 模块 | 端口 | 主题 |
|---|---|---|
| [langchain4j-agentic-basics](langchain4j-agentic/langchain4j-agentic-basics/README.md) | 8716 | @Agent + sequenceBuilder + AgenticScope |
| [langchain4j-agentic-loop](langchain4j-agentic/langchain4j-agentic-loop/README.md) | 8717 | loopBuilder + exitCondition 打分迭代 |
| [langchain4j-agentic-parallel](langchain4j-agentic/langchain4j-agentic-parallel/README.md) | 8718 | parallelBuilder + output 聚合 |
| [langchain4j-agentic-supervisor](langchain4j-agentic/langchain4j-agentic-supervisor/README.md) | 8719 | supervisorBuilder 主管编排（银行工具） |
| [langchain4j-agentic-recovery](langchain4j-agentic/langchain4j-agentic-recovery/README.md) | 8720 | errorHandler 缺参补值重试 |
| [langchain4j-agentic-observability](langchain4j-agentic/langchain4j-agentic-observability/README.md) | 8721 | AgentMonitor 调用树 + HTML 报告 |

### ⑧ [mcp](langchain4j-mcp/README.md) — 端口 8722-8723

| 模块 | 端口 | 主题 |
|---|---|---|
| [langchain4j-mcp-stdio](langchain4j-mcp/langchain4j-mcp-stdio/README.md) | 8722 | MCP stdio 客户端（连外部 server） |
| [langchain4j-mcp-ai-service](langchain4j-mcp/langchain4j-mcp-ai-service/README.md) | 8723 | McpToolProvider 注入 AiServices |

### ⑨ [patterns](langchain4j-patterns/README.md) — 端口 8724-8726

| 模块 | 端口 | 模式 |
|---|---|---|
| [langchain4j-pattern-routing](langchain4j-patterns/langchain4j-pattern-routing/README.md) | 8724 | conditionalBuilder 路由（medical/legal/technical） |
| [langchain4j-pattern-parallel](langchain4j-patterns/langchain4j-pattern-parallel/README.md) | 8725 | parallelBuilder 并发 + output 聚合 |
| [langchain4j-pattern-supervisor](langchain4j-patterns/langchain4j-pattern-supervisor/README.md) | 8726 | supervisorBuilder 主管编排 |

### ⑩ capstone — 端口 8730

| 模块 | 端口 | 主题 |
|---|---|---|
| [langchain4j-capstone-app](langchain4j-capstone/langchain4j-capstone-app/README.md) | 8730 | 智能客服综合（RAG + memory + tools + agentic） |

## 环境变量

| 变量 | 用途 |
|---|---|
| `DEEPSEEK_API_KEY` | 全部模块的聊天/推理模型（DeepSeek baseUrl） |

RAG 嵌入使用本地 ONNX（BGE-zh），无任何 API key 依赖。

## 技术栈

- Java 21
- Spring Boot 4.0.3（父 pom；starter 集成件官方构建于 Boot 3.5.13，已验证手动 Bean 等价方案）
- LangChain4j 1.20.0（BOM）+ agentic/mcp/spring 集成件 1.20.0-beta30
- BGE-small-zh 本地嵌入 1.9.0-beta16
- DeepSeek API（OpenAI 兼容）
