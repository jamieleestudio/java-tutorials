# ② 中间件 ★（agentscope-middleware）

## 这一章解决什么

AgentScope 独有的 Middleware 链机制——在 ReAct 循环的每个阶段插入自定义逻辑：
日志、预算控制、上下文压缩、提示改写。

## 模块清单

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-middleware-basics](agentscope-middleware-basics/README.md) | 9105 | 中间件基础（5 个钩子 + 改写系统提示） | `GET /middleware/enhanced` |
| [agentscope-custom-budget](agentscope-custom-budget/README.md) | 9106 | 自实现预算熔断（BudgetControlMiddleware） | `GET /budget/ask` |
| [agentscope-context-compaction](agentscope-context-compaction/README.md) | 9107 | 上下文压缩（CompactionMiddleware + CompactionConfig） | `GET /compaction/ask` |

## 核心概念

**MiddlewareBase** 有 5 个钩子（洋葱模型）：

| 钩子 | 时机 | 用途 |
|---|---|---|
| `onAgent` | Agent 调用入口 | 日志/认证/审计 |
| `onReasoning` | 每轮推理（调模型前） | 预算检查/循环限制 |
| `onActing` | 每轮行动（调工具前） | 权限检查/工具拦截 |
| `onModelCall` | 每次 LLM 调用 | token 统计/缓存 |
| `onSystemPrompt` | 系统提示生成 | 提示改写/注入 |

**短路**：不调 `next.apply(input)` 即跳过后续逻辑。

## 与 Embabel 的对照

| 概念 | Embabel | AgentScope |
|---|---|---|
| 扩展点 | `@Action`（声明式动作） | `MiddlewareBase`（循环钩子） |
| 预算控制 | `ProcessOptions.withBudget()` 内置 | 自行实现（`BudgetControlMiddleware`） |
| 上下文压缩 | 无（process 无状态） | `CompactionMiddleware` 内置 |