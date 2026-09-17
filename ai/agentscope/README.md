# AgentScope Java 教程

用 **Java 21 + Spring Boot 4.0.3 + AgentScope 2.0.3** 演示 AgentScope Java 的核心能力与独有特性。

> AgentScope 是阿里巴巴开源的 Agent 框架（Python 2.0.8 / Java 2.0.3）。
> Java 版（`io.agentscope`）在 Maven Central 上发布，核心 API 框架无关（core/harness 不依赖 Spring），
> 但提供了 Spring Boot 4 starter 用于自动配置。本教程用 Java 版，模型接入 DeepSeek（OpenAI 兼容接口）。

## 与 Embabel 的关系

本仓库的 `ai/embabel`（52 模块）用 Embabel 1.0.0 + Spring Boot 3.5 演示"类型化建模 + GOAP 规划器"范式。
AgentScope 是另一种范式：**ReAct 循环 + Middleware 链 + Permission 引擎 + Workspace/Sandbox**——
不靠规划器推导动作，而是让模型自主决策、用中间件做约束。

两者用不同的 Spring Boot 版本（Embabel 3.5 / AgentScope 4.0.3），互不影响。

## 分类与模块（36 模块 / 8 分类，全部编译通过）

### ① [基础（agentscope-basics）](agentscope-basics/README.md)

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-chat](agentscope-basics/agentscope-chat/README.md) | 9100 | 最小聊天 Agent（HarnessAgent + DeepSeek） | `GET /ai/generate` |
| [agentscope-tools](agentscope-basics/agentscope-tools/README.md) | 9101 | 工具调用（@Tool + Toolkit） | `GET /tools/ask` |
| [agentscope-structured-output](agentscope-basics/agentscope-structured-output/README.md) | 9102 | 结构化输出（call + Class） | `GET /structured/ask` |
| [agentscope-streaming](agentscope-basics/agentscope-streaming/README.md) | 9103 | 流式输出（Flux + Event） | `GET /stream/ask` |
| [agentscope-model-providers](agentscope-basics/agentscope-model-providers/README.md) | 9104 | 模型提供商（OpenAI/DeepSeek/Ollama） | `GET /model/ask` |

### ② 中间件 ★ [agentscope-middleware](agentscope-middleware/README.md)

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-middleware-basics](agentscope-middleware/agentscope-middleware-basics/README.md) | 9105 | 中间件基础（5 个钩子 + 改写系统提示） | `GET /middleware/enhanced` |
| [agentscope-custom-budget](agentscope-middleware/agentscope-custom-budget/README.md) | 9106 | 自实现预算熔断（用 Middleware 补 Java 缺口） | `GET /budget/ask` |
| [agentscope-context-compaction](agentscope-middleware/agentscope-context-compaction/README.md) | 9107 | 上下文压缩（CompactionMiddleware + CompactionConfig） | `GET /compaction/ask` |

### ③ 工具与技能 ★ [agentscope-tools-skills](agentscope-tools-skills/README.md)

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-toolkit-groups](agentscope-tools-skills/agentscope-toolkit-groups/README.md) | 9108 | 工具分组（ToolGroup + group registration） | `GET /toolgroup/ask` |
| [agentscope-coding-tools](agentscope-tools-skills/agentscope-coding-tools/README.md) | 9109 | 编码工具（ShellExecuteTool + FilesystemTool） | `GET /coding/ask` |
| [agentscope-mcp](agentscope-tools-skills/agentscope-mcp/README.md) | 9110 | MCP 客户端（McpClientBuilder SSE/stdio/HTTP） | `GET /mcp/ask` |
| [agentscope-skills](agentscope-tools-skills/agentscope-skills/README.md) | 9111 | 技能系统（SkillBox + AgentSkill + WorkspaceSkillRepository） | `GET /skills/ask` |
| [agentscope-skill-curator](agentscope-tools-skills/agentscope-skill-curator/README.md) | 9112 | 技能策展（SkillCurator + SkillPromoter 自动晋升） | `GET /curator/ask` |
| [agentscope-subagent](agentscope-tools-skills/agentscope-subagent/README.md) | 9113 | 子代理（SubagentDeclaration + Agent-as-Tool） | `GET /subagent/ask` |
| [agentscope-background-tasks](agentscope-tools-skills/agentscope-background-tasks/README.md) | 9114 | 后台任务（TaskTool + TaskRepository） | `GET /background/ask` |

### ④ 权限与 HITL ★ [agentscope-permission](agentscope-permission/README.md)

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-permission-modes](agentscope-permission/agentscope-permission-modes/README.md) | 9115 | 权限模式（DEFAULT/ACCEPT_EDITS/EXPLORE/BYPASS/DONT_ASK） | `GET /permission/default` |
| [agentscope-permission-rules](agentscope-permission/agentscope-permission-rules/README.md) | 9116 | 权限规则（PermissionRule + PermissionEngine） | `GET /permission/rules` |
| [agentscope-hitl-confirm](agentscope-permission/agentscope-hitl-confirm/README.md) | 9117 | 人工确认（PermissionBehavior.ASK） | `GET /hitl/confirm` |
| [agentscope-interrupt](agentscope-permission/agentscope-interrupt/README.md) | 9118 | 实时打断（Agent.interrupt()） | `GET /interrupt/start` |

### ⑤ 工作区与沙箱 ★ [agentscope-workspace](agentscope-workspace/README.md)

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-local-workspace](agentscope-workspace/agentscope-local-workspace/README.md) | 9119 | 本地工作区（LocalFilesystem + LocalFilesystemSpec） | `GET /workspace/local` |
| [agentscope-docker-sandbox](agentscope-workspace/agentscope-docker-sandbox/README.md) | 9120 | Docker 沙箱（DockerFilesystemSpec） | `GET /sandbox/ask` |
| [agentscope-remote-filesystem](agentscope-workspace/agentscope-remote-filesystem/README.md) | 9121 | 远程文件系统（RemoteFilesystem + InMemoryStore） | `GET /workspace/remote` |
| [agentscope-workspace-snapshot](agentscope-workspace/agentscope-workspace-snapshot/README.md) | 9122 | 工作区快照（SandboxSnapshotSpec） | `GET /workspace/snapshot` |
| [agentscope-plan-mode](agentscope-workspace/agentscope-plan-mode/README.md) | 9123 | 计划模式（enterPlanMode / exitPlanMode） | `GET /plan/ask` |

### ⑥ 记忆与知识 [agentscope-memory-rag](agentscope-memory-rag/README.md)

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-memory](agentscope-memory-rag/agentscope-memory/README.md) | 9124 | 对话记忆（MemoryConfig + MemoryFlushMiddleware） | `GET /memory/ask` |
| [agentscope-longterm-memory](agentscope-memory-rag/agentscope-longterm-memory/README.md) | 9125 | 长期记忆（LongTermMemory + StaticLongTermMemoryHook） | `GET /longterm/ask` |
| [agentscope-rag](agentscope-memory-rag/agentscope-rag/README.md) | 9126 | RAG 检索（Knowledge + KnowledgeRetrievalTools） | `GET /rag/ask` |
| [agentscope-rag-backends](agentscope-memory-rag/agentscope-rag-backends/README.md) | 9127 | RAG 后端选择（InMemory/Qdrant/Milvus/Chroma） | `GET /rag/backends` |
| [agentscope-persistence](agentscope-memory-rag/agentscope-persistence/README.md) | 9128 | 会话持久化（AgentStateStore） | `GET /persistence/chat` |

### ⑦ 服务与渠道 ★ [agentscope-service](agentscope-service/README.md)

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-gateway](agentscope-service/agentscope-gateway/README.md) | 9129 | 网关（HarnessGateway + GatewayBootstrap） | `GET /gateway/routed` |
| [agentscope-chatui](agentscope-service/agentscope-chatui/README.md) | 9130 | ChatUI 渠道（ChatUiChannel send/poll） | `GET /chatui/send` |
| [agentscope-channels](agentscope-service/agentscope-channels/README.md) | 9131 | 多渠道路由（ChannelManager + Channel） | `GET /channels/ask` |
| [agentscope-scheduler](agentscope-service/agentscope-scheduler/README.md) | 9132 | 调度器（WakeupDispatcher + MessageBus） | `GET /scheduler/ask` |
| [agentscope-a2a](agentscope-service/agentscope-a2a/README.md) | 9133 | A2A 协议（RemoteTarget + RemoteSubagentStub） | `GET /a2a/ask` |

### ⑧ 综合 [agentscope-capstone](agentscope-capstone/README.md)

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-coding-agent](agentscope-capstone/agentscope-coding-agent/README.md) | 9134 | 编码 Agent（整合 Shell+File+Compaction+Memory+Permission） | `GET /capstone/coding` |
| [agentscope-capstone-e2e](agentscope-capstone/agentscope-capstone-e2e/README.md) | 9135 | 端到端多 Agent 系统（Subagent+Memory+Gateway） | `GET /capstone/e2e` |

## 快速开始

```bash
# 编译所有模块
mvn package -DskipTests -pl ai/agentscope -am

# 运行单个模块（需要 DeepSeek API key）
cd ai/agentscope/agentscope-basics/agentscope-chat
mvn spring-boot:run -Dspring-boot.run.arguments="--agentscope.model.api-key=YOUR_KEY"
```

## 关键 API

| 能力 | 核心类 | 构建方式 |
|---|---|---|
| Agent | `HarnessAgent` | `HarnessAgent.builder().name().model().build()` |
| 中间件 | `MiddlewareBase` | `.middleware(new MyMiddleware())` |
| 预算控制 | `BudgetControlMiddleware`（自实现） | `.middleware(new BudgetControlMiddleware(maxTokens, maxIters))` |
| 上下文压缩 | `CompactionConfig` | `.compaction(CompactionConfig.builder().triggerMessages(8).build())` |
| 工具 | `@Tool` + `@ToolParam` + `Toolkit` | `toolkit.registerTool(obj)` |
| MCP | `McpClientBuilder` | `McpClientBuilder.create(name).stdioTransport(cmd).buildAsync()` |
| 技能 | `AgentSkill` + `SkillBox` | `.skillRepository(repo)` |
| 子代理 | `SubagentDeclaration` | `.subagent(SubagentDeclaration.builder().name().build())` |
| 权限 | `PermissionContextState` | `.permissionContext(PermissionContextState.builder().mode().build())` |
| 打断 | `Agent.interrupt()` | `agent.interrupt()` |
| 计划模式 | `enterPlanMode/exitPlanMode` | `.enablePlanMode()` |
| 记忆 | `MemoryConfig` | `.memory(MemoryConfig.builder().build())` |
| RAG | `Knowledge` + `KnowledgeRetrievalTools` | `toolkit.registerTool(new KnowledgeRetrievalTools(knowledge))` |
| 网关 | `HarnessGateway` | `HarnessGateway.create(channelManager)` |

## 技术栈

- Java 21
- Spring Boot 4.0.3
- AgentScope 2.0.3（BOM）
- DeepSeek API（OpenAI 兼容）
- Reactor（Mono/Flux 响应式）