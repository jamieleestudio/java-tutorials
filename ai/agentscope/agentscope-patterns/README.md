# ⑨ Agent 模式 ★（agentscope-patterns）

## 这一章解决什么

**同一个 LLM，用不同的结构组织，效果和成本差很多。** 这一章把主流 Agent 编排模式逐一落地，
与 `ai/embabel/embabel-patterns`（19 模块）**1:1 对照**——
同一个模式用 AgentScope 的 ReAct + Middleware + Subagent + Permission 范式重新实现。

> **核心差异**：Embabel 用 GOAP 规划器推导动作序列（声明式），AgentScope 用 ReAct 循环让模型自主决策（命令式 + 中间件约束）。
> 同一个模式，两种范式的实现方式完全不同——这就是本章的价值。

## 与 Embabel 的范式对照

| 维度 | Embabel（GOAP 范式） | AgentScope（ReAct 范式） |
|---|---|---|
| 编排核心 | `@Agent` + `@Action` + `AgentInvocation` | `HarnessAgent.builder()` + `.call()` |
| 动作决策 | 规划器推导动作序列 | 模型在 ReAct 循环中自主选择工具 |
| 流程控制 | `@Condition` / `goal` / `flow` | Middleware 短路 / PlanMode / maxIters |
| 分支路由 | `@Condition` 选择 path | 模型选择不同工具 / Routing 工具 |
| 并行 | Kotlin `ScatterGather` / `Consensus` | 多个 Subagent 并发 `Flux.merge` |
| 委派 | `SubagentProcess` 嵌套调用 | `SubagentDeclaration` + Agent-as-Tool |
| 状态管理 | Process 状态 + `ProcessControl` | `AgentState` + `AgentStateStore` |
| 错误恢复 | `@Action(canRerun=true)` + rerun | Middleware 拦截 + maxRetries |
| 约束 | `ProcessOptions.withBudget()` | `BudgetControlMiddleware`（自实现） |

## 模块清单（19 模块 / 端口 9200-9219）

### 工作流模式（路径可预测）

| # | 模块 | 端口 | 模式 | Embabel 对照 | AgentScope 实现要点 |
|---|---|---|---|---|---|
| 1 | `agentscope-prompt-chaining` | 9200 | Prompt chaining + 关卡 | `embabel-prompt-chaining` | 多次 `agent.call()`，每次输出作为下次输入；关卡用 Middleware 检查输出，不通过则 `Flux.empty()` 短路 |
| 2 | `agentscope-routing` | 9201 | 路由分类 | `embabel-routing` | 第一个 Agent 做结构化输出（分类标签），switch 到不同专用 Agent；或用一个路由工具根据输入选择子 Agent |
| 3 | `agentscope-parallelization` | 9202 | 并行（Sectioning / Voting） | `embabel-parallelization` | `Flux.merge(agentA.call(msg), agentB.call(msg))` 并发多 Agent；Voting 模式收集多个结果投票 |
| 4 | `agentscope-orchestrator-workers` | 9203 | 编排者-工人 | `embabel-orchestrator-workers` | 主 Agent 用 `SubagentDeclaration` 声明多个工人子 Agent，通过 `AgentSpawnTool` 动态分配任务 |
| 5 | `agentscope-refinement` | 9204 | 自评迭代 | `embabel-refinement` | 循环调用：生成 → 评估 → 改进，用 `maxIters` 控制迭代次数，Middleware 检查评估分数决定是否继续 |

### Agent 模式（模型自主决策）

| # | 模块 | 端口 | 模式 | Embabel 对照 | AgentScope 实现要点 |
|---|---|---|---|---|---|
| 6 | `agentscope-autonomous-agent` | 9205 | 自主 Agent | `embabel-autonomous-agent` | ReAct 循环（AgentScope 原生）+ 工具集 + `maxIters` 停止条件 + 错误恢复（`ExecutionConfig.maxAttempts`） |
| 7 | `agentscope-supervisor` | 9206 | 主管编排 | `embabel-supervisor` | 主 Agent 作为 supervisor，通过 `AgentSpawnTool` / `agentSend` 管理多个子 Agent 的任务分配和结果收集 |
| 8 | `agentscope-replanning` | 9207 | 动态重规划 | `embabel-replanning` | 工具失败后 Middleware 拦截错误，注入"重规划"提示消息，让模型在下一轮推理中重新选择路径 |
| 9 | `agentscope-multi-goal` | 9208 | 多目标选择 | `embabel-multi-goal` | 多个结构化输出目标，Agent 根据用户意图选择最优目标；用 `call(msgs, Class<?>)` 拿到不同目标类型 |
| 10 | `agentscope-trigger` | 9209 | 反应式触发 | `embabel-trigger` | `Hook.onEvent(PRE_CALL)` 在 Agent 调用前检查触发条件；或 `WakeupDispatcher` 监听 `MessageBus` 事件触发 Agent |

### 协作与工具模式

| # | 模块 | 端口 | 模式 | Embabel 对照 | AgentScope 实现要点 |
|---|---|---|---|---|---|
| 11 | `agentscope-subagent-handoff` | 9210 | 子 Agent 委派 | `embabel-subagent` | `SubagentDeclaration` + `WorkspaceMode.SHARED`；主 Agent 通过工具调用子 Agent，结果回传 |
| 12 | `agentscope-tools-advanced` | 9211 | 渐进式工具 + 自省 | `embabel-tools-advanced` | `ToolGroup` 按阶段激活工具组；`SkillBox` 动态注入技能；Middleware 在 `onActing` 中纠正工具 |
| 13 | `agentscope-tool-chaining` | 9212 | 工具链式展开 | `embabel-tool-chaining` | `ToolGroup` + `SkillToolGroup`：前一个工具的结果产生领域对象 → 激活该对象的专属工具组 |
| 14 | `agentscope-playbook` | 9213 | 条件式工具集 | `embabel-playbook` | `SkillFilter.only("skillA","skillB")` 按前置条件收敛可用技能；`enableSkills()` / `disableSkills()` 动态切换 |
| 15 | `agentscope-capstone-patterns` | 9214 | 端到端综合 | `embabel-capstone` | 整合 Middleware + Permission + Budget + Subagent + Memory + Workspace + RAG 的完整系统 |
| 16 | `agentscope-debate` | 9215 | 多 Agent 辩论 | `embabel-debate` | 两个 Agent 持对立 prompt（正方/反方）+ 第三个 Agent 当裁判；`Flux.merge` 并发正反方，串行裁判 |
| 17 | `agentscope-tree-of-thoughts` | 9216 | 思维树 | `embabel-tree-of-thoughts` | 多个 Agent 用不同 temperature 生成候选方案 → 评估 Agent 评分 → 选最优 → 深化 |
| 18 | `agentscope-state-machine` | 9217 | 状态机 | `embabel-state-machine` | `PlanMode` 做只读分析阶段 → `exitPlanMode` 切换到执行阶段；`SkillFilter` 按阶段收敛工具集 |
| 19 | `agentscope-programmatic-dsl` | 9218 | 编程式 DSL | `embabel-programmatic-dsl` | AgentScope Java 没有 Kotlin DSL（Embabel 有），对照展示纯 Java Builder 式构造：`HarnessAgent.builder()...middleware()...subagent()...build()` |

## 建议阅读顺序

1. **先看清"结构"**：`prompt-chaining` → `routing` → `parallelization` → `orchestrator-workers`
   - 看 AgentScope 的 ReAct 循环如何替代 Embabel 的 GOAP 规划器
2. **再看"自主"**：`autonomous-agent` → `supervisor` → `replanning`
   - 看 Middleware 如何约束自主 Agent
3. **最后补"协作与工具"**：`subagent` → `tools-advanced` → `tool-chaining` → `playbook`
   - 看 SubagentDeclaration 和 ToolGroup 如何实现渐进式工具
4. 想深入"迭代改进"看 `refinement`；想看"多 Agent 协作"看 `debate` 和 `tree-of-thoughts`
5. 想加"硬约束"看 `state-machine`（PlanMode）；想看 Builder 链式构造看 `programmatic-dsl`
6. 最终综合看 `capstone`

## 与相邻分类的边界

- **本章是"结构模式"**；前 8 个分类（basics → capstone）是"能力 API"。
- **`agentscope-middleware`（②）与本章**：前者讲 Middleware 机制本身（5 个钩子），
  本章讲**用 Middleware 实现编排模式**（短路=关卡、拦截=重规划、注入=提示改写）。
- **`agentscope-tools-skills/agentscope-subagent`（③）与本章 `agentscope-subagent-handoff`（⑨）**：
  前者讲 SubAgentTool 的 API 用法，本章讲**用子 Agent 实现 handoff 委派模式**。
- **`agentscope-capstone`（⑧）与本章 `agentscope-capstone-patterns`（⑨）**：
  前者是"能力整合"（Shell+File+Memory+Permission），本章是"模式整合"（orchestrator+subagent+budget+RAG）。

## 端口规划

| 端口段 | 分类 |
|---|---|
| 9100-9135 | agentscope 基础 → capstone（已完成） |
| 9200-9219 | **agentscope-patterns**（本章，19 模块） |