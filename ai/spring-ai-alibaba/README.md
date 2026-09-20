# Spring AI Alibaba 教程

用 **Java 21 + Spring Boot 4.0.3 + Spring AI Alibaba 2.0.0-M1.1** 演示 SAA 的核心能力：
Graph 图编排、Agent Framework（ReactAgent）、RAG、MCP 与编排模式。

> Spring AI Alibaba（SAA）是 Spring AI 的超集：`ChatClient` / `ChatModel` 等核心抽象完全一致，
> 在此之上提供 **Graph 编排框架**（源自 LangGraph 思想）、**Agent Framework**（ReactAgent + Hook/Interceptor）、
> DashScope 模型接入与 Nacos 生态集成。
>
> ⚠️ 版本说明：SAA 2.0.0-M1.1 官方配对 **Spring AI 2.0.0-M1（里程碑版）**，
> 其 ChatClient 内部 API 与 GA 2.0.0 不兼容。因此本组的 Graph / Agent / Patterns / Capstone
> 模块在各自 POM 中显式锁定 `spring-ai 2.0.0-M1`；纯 Spring AI 模块
> （basics / rag / mcp / observability）使用 GA 2.0.0，组内两套版本互不干扰。

## 与其他三组的定位差异

| 维度 | Spring AI Alibaba | Spring AI | Embabel | AgentScope |
|---|---|---|---|---|
| 定位 | Spring AI 超集 + Graph/Agent 框架 | 通用 AI 集成框架 | 类型化建模 + GOAP 规划 | ReAct 循环 + Middleware |
| 核心抽象 | `StateGraph` + `ReactAgent` | `ChatClient` + Advisor | `@Agent` + `@Action` | `HarnessAgent` + Middleware |
| 编排 | 图（节点/边/条件/checkpoint） | 手写代码 | GOAP 规划器推导 | ReAct + 中间件约束 |
| 记忆 | checkpoint saver + threadId | ChatMemory + Advisor | Process 状态 | AgentStateStore |
| HITL | `interruptBefore` + `resume` | — | HITL Action | Permission/ASK |

## 分类与模块（35 模块 / 8 分类，全部编译通过，核心链路已运行验证）

### ① [基础（spring-ai-alibaba-basics）](spring-ai-alibaba-basics/README.md) — 端口 8500-8503

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [spring-ai-alibaba-chat](spring-ai-alibaba-basics/spring-ai-alibaba-chat/README.md) | 8500 | 基础聊天（对照 spring-ai-chat） | `GET /ai/chat` |
| [spring-ai-alibaba-streaming](spring-ai-alibaba-basics/spring-ai-alibaba-streaming/README.md) | 8501 | 流式输出（Flux + SSE） | `GET /ai/streaming` |
| [spring-ai-alibaba-structured-output](spring-ai-alibaba-basics/spring-ai-alibaba-structured-output/README.md) | 8502 | 结构化输出 | `GET /ai/structured` |
| [spring-ai-alibaba-multi-model](spring-ai-alibaba-basics/spring-ai-alibaba-multi-model/README.md) | 8503 | 多模型（DeepSeek + 通义千问兼容模式） | `GET /model/ask` |

### ② [Graph 图编排 ★（spring-ai-alibaba-graph）](spring-ai-alibaba-graph/README.md) — 端口 8504-8509

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [spring-ai-alibaba-graph-basics](spring-ai-alibaba-graph/spring-ai-alibaba-graph-basics/README.md) | 8504 | StateGraph + 节点/边 + Mermaid | `GET /graph/run` |
| [spring-ai-alibaba-graph-conditional](spring-ai-alibaba-graph/spring-ai-alibaba-graph-conditional/README.md) | 8505 | 条件边路由 | `GET /graph/route` |
| [spring-ai-alibaba-graph-streaming](spring-ai-alibaba-graph/spring-ai-alibaba-graph-streaming/README.md) | 8506 | 图级流式（逐节点事件） | `GET /graph/stream` |
| [spring-ai-alibaba-graph-checkpoint](spring-ai-alibaba-graph/spring-ai-alibaba-graph-checkpoint/README.md) | 8507 | checkpoint + threadId + 状态历史 | `GET /graph/checkpoint/chat` |
| [spring-ai-alibaba-graph-human-feedback](spring-ai-alibaba-graph/spring-ai-alibaba-graph-human-feedback/README.md) | 8508 | HITL（interruptBefore + resume） | `POST /graph/hitl/start` |
| [spring-ai-alibaba-graph-subgraph](spring-ai-alibaba-graph/spring-ai-alibaba-graph-subgraph/README.md) | 8509 | 子图组合 | GET /graph/subgraph/run |
| [spring-ai-alibaba-graph-tool-hitl](spring-ai-alibaba-graph/spring-ai-alibaba-graph-tool-hitl/README.md) | 8547 | 工具级 HITL 审批 | GET /agent/tool-hitl/start |
| [spring-ai-alibaba-graph-store](spring-ai-alibaba-graph/spring-ai-alibaba-graph-store/README.md) | 8548 | 长期记忆 Store | GET /graph/store/chat |
| [spring-ai-alibaba-graph-file-saver](spring-ai-alibaba-graph/spring-ai-alibaba-graph-file-saver/README.md) | 8549 | checkpoint 落盘恢复 | GET /graph/file-saver/chat |

### ③ [Agent Framework ★（spring-ai-alibaba-agent）](spring-ai-alibaba-agent/README.md) — 端口 8510-8513

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [spring-ai-alibaba-react-agent](spring-ai-alibaba-agent/spring-ai-alibaba-react-agent/README.md) | 8510 | ReactAgent + ModelCallLimitHook | `GET /agent/ask` |
| [spring-ai-alibaba-agent-tools](spring-ai-alibaba-agent/spring-ai-alibaba-agent-tools/README.md) | 8511 | 工具调用（@Tool + methodTools） | `GET /agent/tools/ask` |
| [spring-ai-alibaba-agent-memory](spring-ai-alibaba-agent/spring-ai-alibaba-agent-memory/README.md) | 8512 | 会话记忆（saver + threadId） | `GET /agent/memory/chat` |
| [spring-ai-alibaba-agent-multi-agent](spring-ai-alibaba-agent/spring-ai-alibaba-agent-multi-agent/README.md) | 8513 | 多 Agent（SubAgentInterceptor 委派） | GET /agent/multi/ask |
| [spring-ai-alibaba-agent-filesystem](spring-ai-alibaba-agent/spring-ai-alibaba-agent-filesystem/README.md) | 8540 | 内置文件工具套件（类 Claude Code） | GET /agent/fs/ask |
| [spring-ai-alibaba-agent-shell](spring-ai-alibaba-agent/spring-ai-alibaba-agent-shell/README.md) | 8541 | Shell 命令执行 | GET /agent/shell/ask |
| [spring-ai-alibaba-agent-todos](spring-ai-alibaba-agent/spring-ai-alibaba-agent-todos/README.md) | 8542 | 任务清单（WriteTodosTool） | GET /agent/todos/ask |
| [spring-ai-alibaba-agent-hooks](spring-ai-alibaba-agent/spring-ai-alibaba-agent-hooks/README.md) | 8543 | Hook 体系（摘要/PII/限流） | GET /agent/hooks/summary |
| [spring-ai-alibaba-agent-skills](spring-ai-alibaba-agent/spring-ai-alibaba-agent-skills/README.md) | 8544 | 技能体系（SKILL.md） | GET /agent/skills/ask |
| [spring-ai-alibaba-agent-scheduling](spring-ai-alibaba-agent/spring-ai-alibaba-agent-scheduling/README.md) | 8545 | 定时调度 | GET /agent/schedule/status |
| [spring-ai-alibaba-agent-a2a](spring-ai-alibaba-agent/spring-ai-alibaba-agent-a2a/README.md) | 8546 | A2A 远程互操作 | GET /agent/a2a/ask |

### ④ [RAG（spring-ai-alibaba-rag）](spring-ai-alibaba-rag/README.md) — 端口 8514-8516

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [spring-ai-alibaba-embedding](spring-ai-alibaba-rag/spring-ai-alibaba-embedding/README.md) | 8514 | 向量嵌入 | `GET /ai/embedding` |
| [spring-ai-alibaba-vector-store](spring-ai-alibaba-rag/spring-ai-alibaba-vector-store/README.md) | 8515 | 向量存储（SimpleVectorStore） | `GET /ai/vector/search` |
| [spring-ai-alibaba-rag-basics](spring-ai-alibaba-rag/spring-ai-alibaba-rag-basics/README.md) | 8516 | RAG 三段式 | `GET /ai/rag` |

> 嵌入模型走 OpenAI 兼容端点：默认 DashScope 兼容模式（`text-embedding-v3`），
> 需要 `DASHSCOPE_API_KEY`；可用 `EMBEDDING_BASE_URL` / `EMBEDDING_MODEL` 换任意 OpenAI 兼容嵌入服务。

### ⑤ [MCP（spring-ai-alibaba-mcp）](spring-ai-alibaba-mcp/README.md) — 端口 8517-8518

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [spring-ai-alibaba-mcp-client](spring-ai-alibaba-mcp/spring-ai-alibaba-mcp-client/README.md) | 8517 | MCP 客户端（Streamable HTTP） | `GET /mcp/tools`、`/mcp/ask` |
| [spring-ai-alibaba-mcp-server](spring-ai-alibaba-mcp/spring-ai-alibaba-mcp-server/README.md) | 8518 | MCP 服务端（@Tool 注册工具） | `GET /mcp/info` |

### ⑥ 可观测（spring-ai-alibaba-observability）— 端口 8519

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [spring-ai-alibaba-observability](spring-ai-alibaba-observability/README.md) | 8519 | Micrometer 指标 + Actuator | `GET /observability/run` |

### ⑦ [编排模式 ★（spring-ai-alibaba-patterns）](spring-ai-alibaba-patterns/README.md) — 端口 8520-8523

与其他组的 patterns **1:1 对照**，用 Graph 范式实现：

| 模块 | 端口 | 模式 | 接口 |
|---|---|---|---|
| [spring-ai-alibaba-pattern-routing](spring-ai-alibaba-patterns/spring-ai-alibaba-pattern-routing/README.md) | 8520 | 路由分类 → 分发专家 | `GET /patterns/routing/ask` |
| [spring-ai-alibaba-pattern-parallel](spring-ai-alibaba-patterns/spring-ai-alibaba-pattern-parallel/README.md) | 8521 | 并行（图扇出/扇入） | `GET /patterns/parallel/ask` |
| [spring-ai-alibaba-pattern-supervisor](spring-ai-alibaba-patterns/spring-ai-alibaba-pattern-supervisor/README.md) | 8522 | 主管编排（图循环） | `GET /patterns/supervisor/ask` |
| [spring-ai-alibaba-pattern-capstone](spring-ai-alibaba-patterns/spring-ai-alibaba-pattern-capstone/README.md) | 8523 | 综合（路由+并行+自评迭代） | `GET /patterns/capstone/ask` |

### ⑧ [综合（spring-ai-alibaba-capstone）](spring-ai-alibaba-capstone/README.md) — 端口 8530

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [spring-ai-alibaba-capstone-app](spring-ai-alibaba-capstone/spring-ai-alibaba-capstone-app/README.md) | 8530 | 智能客服（Graph+RAG+工具+HITL） | `GET /capstone/chat` |

## 快速开始

```bash
# 编译所有模块
cd ai/spring-ai-alibaba
mvn package -DskipTests

# 运行单个模块（需要 OPENAI_API_KEY 环境变量 = DeepSeek key）
cd spring-ai-alibaba-graph/spring-ai-alibaba-graph-basics
mvn spring-boot:run

# 体验 MCP 全链路：先启动 8518 服务端，再启动 8517 客户端
```

## 关键 API（SAA 2.0.0-M1.1）

| 能力 | 核心类 | 用法 |
|---|---|---|
| 图定义 | `StateGraph` | `new StateGraph(KeyStrategyFactory)`，KeyStrategy 声明每个 key 的合并策略 |
| 节点 | `AsyncNodeAction.node_async(NodeAction)` | 读 `OverAllState`，返回要合并进状态的数据 |
| 边 | `addEdge` / `addConditionalEdges` | `edge_async(EdgeAction)` 按状态选分支；`addEdge(List, target)` 扇入 |
| 执行 | `CompiledGraph` | `graph.compile()` → `invoke(Map)` / `stream(Map)` |
| checkpoint | `MemorySaver` + `SaverConfig` | `CompileConfig.builder().saverConfig(...)`，执行时带 threadId |
| HITL | `interruptBefore` + `resume` | `updateState(config, feedback)` 后以 `RunnableConfig.resume()` 恢复 |
| 状态恢复 | `updateState` / `getStateHistory` | 回看/改写检查点状态 |
| 智能体 | `ReactAgent.builder()` | `.name().systemPrompt().model().tools()/methodTools()` → `.call(input)` |
| Hook | `ModelCallLimitHook` 等 | `.hooks(...)`，限制调用次数 / 摘要 / PII 等 |
| 子 Agent | `SubAgentInterceptor` | `.addSubAgent(name, agent)`，主管委派专家 |
| 记忆 | `.saver(new MemorySaver())` | `call(input, RunnableConfig.builder().threadId(id).build())` 多轮会话 |
| 多 Agent 工具 | `TaskTool` | Agent-as-Tool 委派 |
| 图可视化 | `getGraph(Type.MERMAID)` | 输出 Mermaid 定义 |

## 技术栈

- Java 21
- Spring Boot 4.0.3
- Spring AI Alibaba 2.0.0-M1.1（BOM）
- Spring AI：Graph/Agent 模块 2.0.0-M1（SAA 官方配对）、纯 Spring AI 模块 2.0.0（GA）
- DeepSeek API（OpenAI 兼容）；嵌入默认 DashScope 兼容模式（可选）
