# AI 模块框架升级记录

> 升级日期：2026-11-11
> 范围：`ai/` 下 5 个框架的版本升级 + 编译验证（242 个叶子模块全部 BUILD SUCCESS）

## 一、升级概览

| 框架 | 升级前 | 升级后 | 官方发布日期 | 说明 |
|---|---|---|---|---|
| LangChain4j | 1.20.0 / beta30 | **1.23.0 / 1.23.0-beta33** | 2026-10-28 | 跨 3 个小版本 |
| Embabel Agent | 1.0.0 | **1.5.4** | 2026-10-28 | 跨 5 个小版本，官方栈 Boot 3.5 → 4.1.1 |
| Spring AI | 2.0.0 | **2.0.2** | 2026-10-23 | 稳定线补丁升级；2.1.0 尚在 RC，未采用 |
| AgentScope Java | 2.0.3 | **2.0.4** | 2026-10-08 | 稳定版；2.1.0-BETA1 未采用 |
| Spring AI Alibaba | 2.0.0-M1.1 | 2.0.0-M1.1（不变） | 2026-09-16 | 已是 Maven Central 最新 |

## 二、各框架新特性速览

### 1. LangChain4j 1.21.0 → 1.23.0（beta 线 beta30 → beta33）

**1.21.0（2026-10-02）— Decision Models 时代开启（@Experimental）**
- 全新 `DecisionModel` API：用「问题 + 类型化答案 + 概率」代替文本生成，适合分类/路由/门控/打分，首个集成是 TypeSafe（兼容 OpenRouter 等实现 `/v1/systemone` 协议的服务）
- `DecisionServices`：像 AI Services 一样用 Java 接口声明决策问题，答案自动转 `boolean`/枚举/`Choice<E>`/record
- 决策模型落地场景：`RoutingChatModel` 模型路由、RAG 重排（`DecisionScoringModel`）、查询路由、工具选择、输入/输出 guardrail
- `DecisionRouterPlanner`：Agentic 模式新路由器，用决策模型选择子 Agent
- 工具错误处理增强：每个异常可自行决定向 LLM 暴露什么信息
- 新增 jitLLM 集成（GPULlama3.java 继任者）、`OpenAiOfficialBatchChatModel`（OpenAI Batch API）

**1.22.0（2026-10-08）**
- OpenAI Decisions API 官方接入：`OpenAiDecisionModel` / `OpenAiOfficialDecisionModel`
- `BedrockBatchChatModel`：AWS Bedrock 批量推理
- Anthropic 自动 prompt caching + cache TTL
- Micrometer 新增 EmbeddingModel 指标监听

**1.23.0（2026-10-28）**
- `OllamaDecisionModel`（支持图片输入）
- 新 Agentic 模式：**Confidence Cascade**（置信度级联）
- **Decision Agents**：决策智能体正式成型
- 可观测性：`AiServiceInvocationSummaryEvent` 汇总一次 AI Service 调用的全部事件
- `langchain4j-test`：LLM judge 与 chat 请求断言（无 key 测试更好写）
- 流式取消语义完善：`cancel()` 后不再触发回调，`StreamingHandle` 可被发现
- MCP 移除废弃的 Jackson 2 API（应用可彻底排除 Jackson 2）；Jackson 升级修复 DoS 漏洞（2.22.4 / 3.2.4）
- 任意 Agent / Agentic 模式可声明激活条件（activation condition）

> **bge 注意**：本仓库使用的 `langchain4j-embeddings-bge-small-zh`（非 v15）官方已停更，最新 1.9.0-beta16，故 `langchain4j-bge.version` 保持不变。若迁移到持续维护的 `langchain4j-embeddings-bge-small-zh-v15`，可跟随 beta 线（1.23.0-beta33）。

### 2. Embabel Agent 1.0.0 → 1.5.4（本仓库变化最大）

**技术栈变迁（官方对齐）**：Spring Boot 3.5 → **4.1.1**、Spring AI 1.x → **2.0.1**、Jackson 2 → **3（tools.jackson）**、Kotlin 2.1.0 → **2.2.21**

- **1.5.0**：Spring AI 2.0.0 GA 迁移；原生结构化输出（strict mode + schema 修复）；DashScope（阿里云百炼）模型支持；供应商中立流式 tool loop；新增 `embabel-agent-starter-byok`；RAG 改进（全文检索分数绝对化、相邻 chunk 获取开关、markdown 表格不切断）
- **1.5.1**：LLM 角色 SPI（角色不再绑定单一 provider）；`SectionReader` 按名称导航文档段落；embedding 驱动的 Skills；BYOK 全量 credential factory；域类型完整支持类型层级
- **1.5.2**：Agent 进程快照/恢复（HITL 持久化基础）；JCache/JDBC 进程持久化 + 自动装配；Agent/Action 延迟策略；LLM 重试与失败事件上报
- **1.5.3**：MCP Server 从 SSE 迁移到 **Streamable HTTP**；Blackboard JDBC 持久化配置；嵌入批量失败对半重试
- **1.5.4**：**类型安全 Decision/Classification Service**（LLM 与 Jev 双后端，声明式决策规格）；Claude 5.x 原生 thinking 支持；OpenAI GPT-6 模型目录 + Responses API；`@EnumAsOneOf` JSON schema 枚举描述；退役 Claude 3.7/Opus 4 常量

> 升级影响：本模块 parent 从 Boot 3.5.0 提升到 4.1.1；两个 Kotlin 模块（parallelization、programmatic-dsl）编译器必须从 2.1.0 升到 2.2.21（旧编译器无法读取新 Kotlin metadata）。

### 3. Spring AI 2.0.0 → 2.0.2

**2.0.1（2026-08-21）**
- 可配置 tool call 调用上限（`ToolCallLimitExceededException`）
- Google GenAI 图片生成；`PagePdfDocumentReader` 支持页码范围；OpenAI 音频语音流式
- 大量修复：流式 tool call delta 按索引合并、tool-search advisor、Milvus/PgVector/OpenSearch 过滤器、OpenAI strict 模式默认 false 等

**2.0.2（2026-10-23）**
- 无状态 MCP server 支持 customizers
- Redis vector store 元数据字段可配置
- `TextToSpeechModel` / `TranscriptionModel` / StabilityAI 图像模型可观测性
- OpenAI strict tool schema 自动补 `additionalProperties:false`；Anthropic 流式 chunk 包含 thinking 文本

**2.1.0 预览（M1 / RC1 / RC2，未采用，等 GA）**
- OpenAI **Responses API**；`VectorStore.upsert`（预计算向量）+ 多个向量库 upsert/query-embedding 搜索
- `MessagePart` 消息模型重构；**session memory**（turn 感知压缩，替代 ChatMemory，后者已标记废弃）
- Spring AI 版 **DecisionModel API**；工具调用确认（tool call confirmation）；MCP "modern"（2026-07-28）协议；流式 tool call 参数 delta

### 4. AgentScope Java 2.0.3 → 2.0.4

**新增**
- OpenAI **Responses API** 模块；`returnDirect` 工具结果短路推理循环
- `AgentRun` / `RunControl`：单次运行级控制（取消/隔离），RuntimeContext 显式传递
- token 用量指标、模型回退 failover 监听、Anthropic bearer token 认证
- MongoDB 存储扩展；E2B 沙箱快照自动清理
- AgentScope Service 重构版；个人微信 iLink 渠道；钉钉 HTTP 回调模式

**破坏性变更（本仓库教程代码未涉及，编译全通过）**
- 移除 `activeRc`、agent 级 `getRuntimeContext()`、`RuntimeContextAware`——`RuntimeContext` 改为通过中间件/工具参数显式传入
- 删除 `RegisteredSkill`、`ToolkitAware`、`agui.model.ToolMergeMode`、`ToolInjection`——工具/技能改为请求级/session 级显式状态
- `HttpTransportConfig#getHttpVersion()` 默认返回 `null`（原 `HTTP_2`），需自行判空

### 5. Spring AI Alibaba（未升级）

- 2.0.0-M1.1（2026-09-16 发布）仍是 Maven Central 最新，无需变更
- 官方配对 Spring AI 2.0.0-M1（里程碑版），本仓库 SAA 模块保持 M1 传递依赖、纯 Spring AI 模块走 GA BOM 的结构不变
- 1.x 线的安全修复（`1.1.2.4-security-fix`、CVE 版本）与本仓库无关（已用 2.x）

## 三、本仓库改动明细

| 文件 | 改动 |
|---|---|
| `ai/langchain4j/pom.xml` | `langchain4j.version` 1.20.0→1.23.0；`langchain4j-beta.version` →1.23.0-beta33；`langchain4j-bge.version` 保持 1.9.0-beta16（停更说明注释） |
| `ai/spring-ai/pom.xml` | `spring-ai.version` 2.0.0→2.0.2（描述/注释同步） |
| `ai/agentscope/pom.xml` | `agentscope.version` 2.0.3→2.0.4（描述/注释同步） |
| `ai/embabel/pom.xml` | parent `spring-boot-starter-parent` 3.5.0→**4.1.1**；`embabel-agent.version` 1.0.0→1.5.4 |
| `ai/embabel/embabel-patterns/embabel-parallelization/pom.xml` | `kotlin.version` 2.1.0→2.2.21 |
| `ai/embabel/embabel-patterns/embabel-programmatic-dsl/pom.xml` | `kotlin.version` 2.1.0→2.2.21 |
| `ai/embabel/embabel-integration/embabel-a2a/pom.xml` | 注释更新（A2A SDK 0.3.2.Final 与官方一致，版本不变） |
| `ai/agentscope/.../agentscope-custom-budget/BudgetAgentController.java` | **顺手修复历史 bug**：Controller 调用了不存在的 `chatUnlimited()/report()`，改为 `chatWithoutBudget()` 与 `usedTokens()+budget()`（与升级无关，升级前即无法编译） |

## 四、验证结果

| 框架 | 模块数 | 结果 |
|---|---|---|
| langchain4j | 39 | BUILD SUCCESS |
| spring-ai | 35 | BUILD SUCCESS |
| agentscope | 64 | BUILD SUCCESS |
| embabel | 61 | BUILD SUCCESS |
| spring-ai-alibaba | 43 | BUILD SUCCESS（未变更，回归验证） |

> 环境备注：编译依赖通过阿里云镜像下载正常；内网 Nexus（10.6.0.243）当前不可达，仅影响该私服特有的内部依赖，中央仓库 artifacts 不受影响。

## 五、新特性实践落地（2026-11-11，P0 + P1）

按规划落地 8 个新模块 + 2 个改造，覆盖 4 个框架的主要新特性（242 + 8 个模块全部编译通过）：

### P0 新范式

| 模块 | 演示特性 | 端口 | 前置条件 |
|---|---|---|---|
| `langchain4j/langchain4j-basics/langchain4j-decision-models` | `DecisionModel` 契约自适配（ChatModel → 决策模型）、`DecisionServices.create` + `@Decide`（boolean/枚举）、`DecisionModelChatModelRouter` 模型路由（1.21/1.22/1.23） | 8701 | DEEPSEEK_API_KEY |
| `langchain4j/langchain4j-patterns/langchain4j-pattern-confidence-cascade` | Confidence Cascade：`decisionAgentBuilder` 决策智能体 + `threshold` + conditional 升级（1.23） | 8727 | DEEPSEEK_API_KEY |
| `embabel/embabel-reasoning/embabel-decision-service` | `Ai.decisions()/classifications()` 类型安全决策/分类服务（1.5.4） | 8941 | DEEPSEEK_API_KEY |
| `agentscope/agentscope-middleware/agentscope-run-control` | `prepareCall` → `AgentRun`（runId/status/cancel/interrupt）、`@Tool(returnDirect=true)` 短路、`fallbackModel + failoverListener`（2.0.4） | 9136 | OPENAI_API_KEY（DeepSeek 兼容） |

### P1 重要增强

| 模块 | 演示特性 | 端口 | 前置条件 |
|---|---|---|---|
| `embabel/embabel-ops/embabel-process-persistence` | `AgentProcessSnapshotStore` Bean 激活进程持久化 + checkpoint 快照观测（1.5.2/1.5.3） | 8943 | DEEPSEEK_API_KEY |
| `embabel/embabel-integration/embabel-mcp-server`（改造） | SSE → **Streamable HTTP** 迁移（单端点 `/mcp`，SDK 2.0 `toolCall` API）（1.5.3） | 8927 | DEEPSEEK_API_KEY |
| `langchain4j/langchain4j-tools/langchain4j-tool-errors` | `LlmVisibleToolExecutionException`：异常自行决定对 LLM 可见性（1.21） | 8702 | DEEPSEEK_API_KEY |
| `langchain4j/langchain4j-agentic/langchain4j-agentic-observability`（扩展） | `AiServiceInvocationSummaryEvent` 一次 AI Service 调用全事件汇总（1.23） | 8721（复用） | DEEPSEEK_API_KEY |
| `spring-ai/spring-ai-tools/spring-ai-tool-limits` | `spring.ai.tools.limits.*` 全局工具调用上限 + `ToolCallLimitBehavior`（2.0.1） | 8027 | OPENAI_API_KEY |
| `agentscope/agentscope-basics/agentscope-responses-api` | `OpenAIResponsesChatModel`（official `/v1/responses` 端点 + 原生结构化输出）（2.0.4） | 9137 | OPENAI_API_KEY（官方） |

### 实现要点（踩坑记录）

- langchain4j 1.23 的 `@Tool` 描述已改为 `value` 属性（不再是 `description`）；`Confidence Cascade` 无独立类，用 `AgenticServices.decisionAgentBuilder(Class)` + `@Decide` + `threshold` 组合实现
- embabel 决策/分类服务由 `LlmDecisionServicesAutoConfiguration` 自动注册，`ServiceSelector`（`defaultService()/named()/byRole()`）取用；答案按 `Proposition/Choice/Rating` 三种 `DecisionAnswer` 模式匹配解析
- MCP Java SDK 2.0 的 server 工具注册从 `.tool(tool, (exchange, args) -> …)` 变为 `.toolCall(tool, (exchange, CallToolRequest) -> …)`，Streamable HTTP 单端点 + `Mcp-Session-Id` 会话
- AgentScope 2.0.4 运行控制：`RuntimeContext` 必须显式传入（隐式 `activeRc` 已移除）；`Toolkit.registerTool(Object)` 注册 `@Tool` 注解工具

## 六、后续可跟进事项

1. **Spring AI 2.1.0 GA 后**再跟进：session memory（替代 ChatMemory）、DecisionModel API、MCP modern 协议、VectorStore.upsert
2. **AgentScope 2.1.0 GA 后**评估 AgentScope Service（Docker/Compose/Helm 部署形态）
3. langchain4j 可考虑将 `bge-small-zh` 迁移到持续维护的 **bge-small-zh-v15**（跟随 beta 线）
4. langchain4j `langchain4j-test` 的 LLM judge（1.23）可作为 testing 专题的新模块（P2，暂缓）
5. P2 清单（暂缓）：embabel DashScope 模块、embabel Claude 5.x thinking 扩展、agentscope MongoDB 存储、spring-ai 无状态 MCP server、langchain4j StreamingHandle 取消语义

