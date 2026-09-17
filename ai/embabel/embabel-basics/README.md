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


## 附：注解速查（未单独建模块的注解）

本仓库没有为每个注解建模块——它们属于"参考手册内容"，看一眼就知道怎么用：

| 注解 | 作用 | 备注 |
|---|---|---|
| `@EmbabelComponent` | 把一个类标记为"组件"，它的动作**不属于任何 Agent**，可被多个 Agent 复用 | 与 `@Agent` 的区别：`@Agent` 会形成一个独立可寻址的 Agent |
| `@Provided` | 注入一个 Spring Bean 作为动作参数 | 已在本章 `embabel-prompts` 演示 |
| `@RequireNameMatch` | 要求参数**按名字**匹配黑板上的绑定，而不是按类型 | 同一类型有多个实例时用 |
| `@Export` | 把动作产出导出到进程外（`Export` 配置） | 需要自定义 `Export` 实现 |
| `@Cost` | 声明动作成本（`cost` 也可写在 `@Action(cost=...)` 上） | 供 GOAP 规划器权衡 |
| `@Semantics` + `@With(key, value)` | 给字段/接口方法加**语义元数据**（如 `predicate`=works at、`inverse`=employs） | 会进入 `PropertyDefinition.metadata`，让规划器理解字段语义而非只有结构 |
| `@CreationPermitted` / `DomainType.getCreationPermitted()` | 控制某个领域类型能否被"创建"（影响规划器是否允许构造该类型） | 与 `DataDictionary` 相关，见 `embabel-planner-types` 的运行时类型一节 |
| `@State` | 类级注解，把"状态对象"作为**类型化对象放进黑板**，其 `@Action` 方法可读写 | 与 `StateMachineTool` 是两套机制，见 `embabel-state-machine` 的对比说明 |

> 判断标准：**需要跑起来才能理解的**才建模块（如 `StuckHandler` 的注册方式、`Budget` 的装配坑）；
> 看一眼就会的写成章节。这也是本仓库没有 100 个模块的原因。
