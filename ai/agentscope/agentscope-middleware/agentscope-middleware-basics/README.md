# agentscope-middleware-basics — 中间件基础 ★

## 演示内容

**AgentScope 独有**：用 `MiddlewareBase` 在 ReAct 循环的每个阶段插入自定义逻辑。

```bash
# 带中间件（系统提示被改写，回答末尾会加「——由中间件增强」）
curl -G --data-urlencode "message=用一句话介绍中间件模式" http://localhost:9105/middleware/enhanced

# 不带中间件（对照组）
curl -G --data-urlencode "message=用一句话介绍中间件模式" http://localhost:9105/middleware/plain
```

> ⚠️ 实测时 DeepSeek 余额不足（HTTP 402），但**中间件日志证明了它确实在循环中被调用**：
> ```
> [middleware] Agent 'middleware-agent' 开始，消息数=1
> [middleware] 系统提示已改写（原长=16，新长=42）
> [middleware] 推理：消息数=2，工具数=23
> ```
> 充值后 `/middleware/enhanced` 的回答末尾会出现「——由中间件增强」，`/middleware/plain` 不会。

## 关键 API

`MiddlewareBase` 有 5 个 default 方法，覆盖整个 ReAct 循环：

| 钩子 | 时机 | 典型用途 |
|---|---|---|
| `onAgent(agent, ctx, AgentInput, next)` | Agent 调用入口（最外层） | 计时、审计 |
| `onReasoning(agent, ctx, ReasoningInput, next)` | 每一轮推理（调模型前） | 改写消息、预算检查 |
| `onActing(agent, ctx, ActingInput, next)` | 每一轮行动（调工具前） | 权限检查、工具过滤 |
| `onModelCall(agent, ctx, ModelCallInput, next)` | 每次 LLM 调用（最内层） | 请求/响应日志 |
| `onSystemPrompt(agent, ctx, prompt)` → `Mono<String>` | 系统提示词生成 | **改写系统提示** |

每个 `on*` 方法的最后一个参数 `next` 是"下一个中间件或核心逻辑"——**洋葱模型**：
你可以在调 `next` 之前/之后做事，也可以**不调 `next` 来短路**（比如预算耗尽时直接返回）。

`order()` 返回执行顺序（数值小的先执行）。

## 与 Embabel 的根本区别

| | Embabel | AgentScope |
|---|---|---|
| 扩展点 | `@Action`（声明式动作） | `MiddlewareBase`（循环钩子） |
| 插入位置 | 动作之间（由规划器决定顺序） | 循环内部（推理前/行动前/模型调用前/提示生成时） |
| 短路能力 | `@Condition`（不满足则不执行该动作） | 不调 `next` → **直接中断整个循环** |
| 改写系统提示 | 无（系统提示是固定的） | `onSystemPrompt` → `Mono<String>` |

**为什么这个区别重要**：Embabel 的扩展是"加新动作"，AgentScope 的扩展是"在循环中间插入逻辑"。
后者能做到前者做不到的事：比如"每次调模型前检查预算"或"每次调工具前检查权限"——
这些是**横切关注点**，不属于某个动作，而是覆盖整个循环。

## 代码结构

- `LoggingMiddleware.java` — 实现 5 个钩子（日志 + 改写系统提示）
- `MiddlewareAgent.java` — `.middleware(new LoggingMiddleware())` 注入
- `MiddlewareController.java` — `/middleware/enhanced`、`/middleware/plain`

## 要点

- **中间件是 AgentScope 2.0 的核心设计**：几乎所有高级能力（permission、compaction、budget、skill）都是通过中间件实现的，而不是硬编码在 Agent 里。
- **洋葱模型**：`next.apply(input)` 是"放行到下一层"，不调就是"短路"——和 Web 框架的 Filter/Interceptor 一样。
- **`onSystemPrompt` 是唯一不返回 `Flux<Event>` 而返回 `Mono<String>` 的钩子**——因为系统提示不是事件流，是一个字符串。
- 框架内置的中间件：`TaskReminderMiddleware`（任务提醒）、`CompactionMiddleware`（上下文压缩）、`AgentTraceMiddleware`（追踪日志）。