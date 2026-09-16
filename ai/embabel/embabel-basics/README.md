# ① 基础（embabel-basics）

## 这一章解决什么

把 Embabel 的"最小可跑通"讲清楚：一个 Agent 长什么样、动作怎么串、模型怎么调、结果怎么变强类型。
**从这里开始**——不引入任何外部组件（不需要 Docker、不需要数据库）。

## 模块清单

| 模块 | 端口 | 主题 |
|---|---|---|
| [embabel-chat](../embabel-basics/embabel-chat/README.md) | 8889 | 最小聊天 Agent（单 Action + `AgentInvocation`） |
| [embabel-planning](../embabel-basics/embabel-planning/README.md) | 8890 | GOAP 多步规划（调研→提纲→成文，动作按类型串联） |
| [embabel-tools](../embabel-basics/embabel-tools/README.md) | 8891 | 工具调用（`@LlmTool` + 函数式 `Tool.create`） |
| [embabel-structured-output](../embabel-basics/embabel-structured-output/README.md) | 8892 | 结构化输出（`creating(T.class)` 强类型数据绑定） |
| [embabel-prompts](../embabel-basics/embabel-prompts/README.md) | 8921 | 提示词工程（Jinja 模板 / `PersonaSpec` / `@Provided`） |

## 建议阅读顺序

1. `embabel-chat` —— 认识 `@Agent` / `@Action` / `@AchievesGoal` / `Ai`
2. `embabel-planning` —— 理解"动作靠类型串联，顺序由规划器推导"
3. `embabel-tools` —— 让模型能调用外部能力
4. `embabel-structured-output` —— 让输出可编程（强类型）
5. `embabel-prompts` —— 把提示词当工程资产管理

## 与相邻分类的边界

- **工具很多、要按需展开** → 见 ⑧ `embabel-tools-advanced`（渐进式工具），不属于基础。
- **要接外部系统（MCP / A2A / 本地模型）** → 见 ⑦ `embabel-integration`。
- **要按模式编排（路由 / 并行 / 主管…）** → 见 ⑧ `embabel-patterns`。
