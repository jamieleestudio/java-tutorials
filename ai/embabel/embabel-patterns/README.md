# ⑧ Agent 模式（embabel-patterns）

## 这一章解决什么

**同一个 LLM，用不同的结构组织，效果和成本差很多。** 这一章把主流 Agent 编排模式逐一落地，
包括 Anthropic《Building Effective Agents》里的全部模式（对照表见总览 README）。

## 模块清单

### 工作流模式（路径可预测）

| 模块 | 端口 | 模式 |
|---|---|---|
| [embabel-prompt-chaining](../embabel-patterns/embabel-prompt-chaining/README.md) | 8916 | Prompt chaining + 关卡（gate 不通过则链条停止） |
| [embabel-routing](../embabel-patterns/embabel-routing/README.md) | 8917 | Routing（分类 → `@Condition` 决定通道） |
| [embabel-parallelization](../embabel-patterns/embabel-parallelization/README.md) | 8918 | Parallelization（Sectioning 并行分片 / Voting 投票） |
| [embabel-orchestrator-workers](../embabel-patterns/embabel-orchestrator-workers/README.md) | 8919 | Orchestrator-workers（动态拆解子任务） |
| [embabel-refinement](../embabel-patterns/embabel-refinement/README.md) | 8899 | Evaluator-optimizer（自评迭代） |
| [embabel-workflows](../embabel-patterns/embabel-workflows/README.md) | 8906 | 编排原语（Kotlin：ScatterGather / Consensus / RepeatUntil） |

### Agent 模式（模型自主决策）

| 模块 | 端口 | 模式 |
|---|---|---|
| [embabel-autonomous-agent](../embabel-patterns/embabel-autonomous-agent/README.md) | 8920 | 自主 Agent（工具循环 + 环境反馈 + 错误恢复 + 停止条件） |
| [embabel-supervisor](../embabel-patterns/embabel-supervisor/README.md) | 8912 | Supervisor（LLM 当主管编排动作） |
| [embabel-replanning](../embabel-patterns/embabel-replanning/README.md) | 8914 | 动态重规划（工具失败换路） |
| [embabel-multi-goal](../embabel-patterns/embabel-multi-goal/README.md) | 8915 | 多目标自动选择（排序器） |
| [embabel-trigger](../embabel-patterns/embabel-trigger/README.md) | 8913 | 反应式触发（`@Action(trigger = X.class)`） |

### 协作与工具模式

| 模块 | 端口 | 模式 |
|---|---|---|
| [embabel-subagent](../embabel-patterns/embabel-subagent/README.md) | 8893 | 子 Agent / handoff 委派 |
| [embabel-tools-advanced](../embabel-patterns/embabel-tools-advanced/README.md) | 8922 | 渐进式工具 + 工具循环回调 + 工具名纠正 |
| [embabel-agentic-tools](../embabel-patterns/embabel-agentic-tools/README.md) | 8928 | Agent 自省工具（查看黑板与进程状态） |
| [embabel-debate](../embabel-patterns/embabel-debate/README.md) | 8930 | 多 Agent 辩论（对立视角 + 裁判综合） |
| [embabel-tree-of-thoughts](../embabel-patterns/embabel-tree-of-thoughts/README.md) | 8931 | 思维树（分支生成 + 评分 + 剪枝 + 取最优） |
| [embabel-state-machine](../embabel-patterns/embabel-state-machine/README.md) | 8929 | 状态机（按状态收敛工具集 + 显式转移） |
| [embabel-programmatic-dsl](../embabel-patterns/embabel-programmatic-dsl/README.md) | 8932 | 编程式 DSL（Kotlin：agent/promptedTransformer/flow/aggregate） |

## 建议阅读顺序

1. **先看清"结构"**：`prompt-chaining` → `routing` → `parallelization` → `orchestrator-workers`
   （从固定路径到动态拆解）
2. **再看"自主"**：`autonomous-agent` → `supervisor` → `replanning`
3. **最后补"协作与工具"**：`subagent` → `tools-advanced` → `agentic-tools`
4. 想深入"迭代改进"看 `refinement`；想用原语自己拼看 `workflows`（Kotlin）
5. 想加"硬约束"看 `state-machine`；想不用注解、纯代码构建 Agent 看 `programmatic-dsl`（Kotlin）

## 与相邻分类的边界

- **本章是"结构模式"**；④ `embabel-reasoning` 是"引擎参数"（规划器类型、模型选择）。
- **`embabel-hitl`（③）与 `embabel-trigger`（本章）**：前者是"等人输入"，后者是"动作触发时机"。
- **`embabel-subagent`（本章，进程内委派）与 `embabel-a2a`（⑦，跨进程协议）**：粒度不同。
- **`embabel-guardrails`/`embabel-secure-tools`（⑤）与 `embabel-state-machine`（本章）**：
  前两者在**执行前后**做校验与权限拦截；状态机是**按阶段**收敛工具的可见性。
