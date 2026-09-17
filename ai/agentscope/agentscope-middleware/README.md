# ② 中间件 ★（agentscope-middleware）

## 模块清单

| 模块 | 端口 | 主题 |
|---|---|---|
| [agentscope-middleware-basics](../agentscope-middleware/agentscope-middleware-basics/README.md) | 9105 | 中间件基础（5 个钩子 + 改写系统提示） |
| [agentscope-context-compaction](../agentscope-middleware/agentscope-context-compaction/README.md) | 9106 | 上下文压缩（CompactionMiddleware + TokenCounter） |
| [agentscope-custom-budget](../agentscope-middleware/agentscope-custom-budget/README.md) | 9107 | 自实现预算熔断（用 Middleware 补 Java 缺口） |
