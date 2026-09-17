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

## 分类与模块（规划中，逐步补充）

### ① [基础（agentscope-basics）](agentscope-basics/README.md)

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-chat](agentscope-basics/agentscope-chat/README.md) | 9100 | 最小聊天 Agent（HarnessAgent + DeepSeek） | `GET /ai/generate` |

### ② 中间件 ★ [agentscope-middleware](agentscope-middleware/README.md)

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-middleware-basics](agentscope-middleware/agentscope-middleware-basics/README.md) | 9105 | 中间件基础（5 个钩子 + 改写系统提示） | `GET /middleware/enhanced` |
| [agentscope-context-compaction](agentscope-middleware/agentscope-context-compaction/README.md) | 9106 | 上下文压缩（CompactionMiddleware + TokenCounter） | `GET /compaction/ask` |
| [agentscope-custom-budget](agentscope-middleware/agentscope-custom-budget/README.md) | 9107 | 自实现预算熔断（用 Middleware 补 Java 缺口） | `GET /budget/ask` |

### ③ 工具与技能 ★ [agentscope-tools-skills](agentscope-tools-skills/README.md)

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-toolkit-groups](agentscope-tools-skills/agentscope-toolkit-groups/README.md) | 9108 | 工具分组（ToolGroup + ToolGroupManager） | `GET /toolgroup/ask` |
| [agentscope-coding-tools](agentscope-tools-skills/agentscope-coding-tools/README.md) | 9109 | 编码工具（Shell + File + Todo） | `GET /coding/ask` |
| [agentscope-mcp](agentscope-tools-skills/agentscope-mcp/README.md) | 9110 | MCP 客户端（SSE/stdio/HTTP） | `GET /mcp/ask` |
| [agentscope-skills](agentscope-tools-skills/agentscope-skills/README.md) | 9111 | 技能系统（SkillBox + SkillRegistry） | `GET /skills/ask` |
| [agentscope-skill-curator](agentscope-tools-skills/agentscope-skill-curator/README.md) | 9112 | 技能策展（自动晋升） | `GET /curator/ask` |
| [agentscope-subagent](agentscope-tools-skills/agentscope-subagent/README.md) | 9113 | 子代理（SubAgentTool） | `GET /subagent/ask` |
| [agentscope-background-tasks](agentscope-tools-skills/agentscope-background-tasks/README.md) | 9114 | 后台任务（BackgroundTask + TaskRepository） | `GET /background/ask` |

### ④ 权限与 HITL ★ [agentscope-permission](agentscope-permission/README.md)

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-permission-modes](agentscope-permission/agentscope-permission-modes/README.md) | 9115 | 权限模式（bypass/confirm/strict） | `GET /permission/modes` |
| [agentscope-permission-rules](agentscope-permission/agentscope-permission-rules/README.md) | 9116 | 权限规则（PermissionRule + PermissionEngine） | `GET /permission/rules` |
| [agentscope-hitl-confirm](agentscope-permission/agentscope-hitl-confirm/README.md) | 9117 | 人工确认（RequireUserConfirmEvent） | `GET /hitl/confirm` |
| [agentscope-interrupt](agentscope-permission/agentscope-interrupt/README.md) | 9118 | 实时打断（Agent.interrupt） | `GET /interrupt/ask` |

### ⑤ 工作区与沙箱 ★ [agentscope-workspace](agentscope-workspace/README.md)

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-local-workspace](agentscope-workspace/agentscope-local-workspace/README.md) | 9119 | 本地工作区（文件系统） | `GET /workspace/local` |
| [agentscope-docker-sandbox](agentscope-workspace/agentscope-docker-sandbox/README.md) | 9120 | Docker 沙箱（隔离执行） | `GET /workspace/docker` |
| [agentscope-remote-filesystem](agentscope-workspace/agentscope-remote-filesystem/README.md) | 9121 | 远程文件系统抽象 | `GET /workspace/remote` |
| [agentscope-workspace-snapshot](agentscope-workspace/agentscope-workspace-snapshot/README.md) | 9122 | 工作区快照/恢复 | `GET /workspace/snapshot` |
| [agentscope-plan-mode](agentscope-workspace/agentscope-plan-mode/README.md) | 9123 | 计划模式（先规划再执行） | `GET /workspace/plan` |

### ⑥ 记忆与知识 [agentscope-memory-rag](agentscope-memory-rag/README.md)

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-memory](agentscope-memory-rag/agentscope-memory/README.md) | 9124 | 内存记忆（InMemoryMemory） | `GET /memory/ask` |
| [agentscope-longterm-memory](agentscope-memory-rag/agentscope-longterm-memory/README.md) | 9125 | 长期记忆（Mem0/ReMe） | `GET /longterm/ask` |
| [agentscope-rag](agentscope-memory-rag/agentscope-rag/README.md) | 9126 | RAG 检索（Knowledge + RAGMode） | `GET /rag/ask` |
| [agentscope-rag-backends](agentscope-memory-rag/agentscope-rag-backends/README.md) | 9127 | RAG 后端（rag-simple） | `GET /rag/backends` |
| [agentscope-persistence](agentscope-memory-rag/agentscope-persistence/README.md) | 9128 | 持久化（state + session） | `GET /persistence/ask` |

### ⑦ 服务与渠道 ★ [agentscope-service](agentscope-service/README.md)

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-gateway](agentscope-service/agentscope-gateway/README.md) | 9129 | 网关（HarnessGateway + ChannelRouter） | `GET /gateway/ask` |
| [agentscope-chatui](agentscope-service/agentscope-chatui/README.md) | 9130 | ChatUI 渠道（浏览器可点） | `GET /chatui/ask` |
| [agentscope-channels](agentscope-service/agentscope-channels/README.md) | 9131 | IM 渠道（钉钉/飞书/企微） | `GET /channels/ask` |
| [agentscope-scheduler](agentscope-service/agentscope-scheduler/README.md) | 9132 | 定时调度（Quartz） | `GET /scheduler/ask` |
| [agentscope-a2a](agentscope-service/agentscope-a2a/README.md) | 9133 | A2A 协议 | `GET /a2a/ask` |

### ⑧ 综合 [agentscope-capstone](agentscope-capstone/README.md)

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-coding-agent](agentscope-capstone/agentscope-coding-agent/README.md) | 9134 | 编码 Agent（Claude Code 式） | `GET /coding-agent/ask` |
| [agentscope-capstone-e2e](agentscope-capstone/agentscope-capstone-e2e/README.md) | 9135 | 端到端综合示例 | `GET /capstone/ask` |