# embabel-tools-advanced — 工具进阶（渐进式工具 / 循环回调 / 工具名纠正 / 自省工具）

## 演示内容

工具多了以后的四类工程问题：

1. **渐进式工具（progressive disclosure）**：把一组工具折叠成一个"门面工具"，模型按需展开
2. **工具循环回调**：观测每轮迭代与每次工具调用（也可用于改写对话历史）
3. **找不到工具的策略**：模型把工具名写错时自动纠正，而不是直接失败
4. **自省工具**：让模型查看**自己**的黑板与进程状态（自省工具本身就是渐进式工具的应用）

## 关键 API

| API | 作用 |
|---|---|
| `@UnfoldingTools(name, description)` | 标在类上，把类里的 `@LlmTool` 折叠成门面工具 |
| `UnfoldingTool.fromInstance(obj)` | 生成门面 `Tool` |
| `@LlmTool(category = "read")` | 用类别分组，让模型按需选择展开哪一组 |
| `withToolLoopInspectors(...)` / `withToolCallInspectors(...)` | 循环级/单次调用级回调（只读观测） |
| `withToolLoopTransformers(...)` | 需要**改写**历史/结果时用（压缩、窗口化） |
| `withToolNotFoundPolicy(new AutoCorrectionPolicy(...))` | 工具名拼错时按相似度自动纠正 |
| `new BlackboardTools().create()` | 自省：查看黑板（共享上下文）里的对象 |
| `new AgentProcessTools().create()` | 自省：查看进程状态、目标、耗时、成本、token、历史动作 |

## 接口

```bash
# 渐进式工具 + 回调 + 工具名纠正
curl -G --data-urlencode "message=帮我看看 A1001 这个订单的状态" http://localhost:8922/tools-advanced/ask

# 自省工具：让模型先确认自己掌握了什么，再回答
curl -G --data-urlencode "message=先查看你当前的黑板与进程状态，再回答：什么是类型化建模？" \
     http://localhost:8922/tools-advanced/inspect
```

实测日志印证了门面机制：

```
(chat) starting tool loop [order_ops] max=20
(chat) calling tool order_ops({"category": "read"})
(chat) tool order_ops returned Tools now available: queryOrder, listOrders. You MUST call one of these ...
LoggingInspector - [tool-call] finished: AfterToolCallContext(...)
DefaultToolLoop - Strategy removed 1 tools after order_ops: [order_ops]     ← 展开后移除门面
```

自省实测输出（模型读到黑板对象与进程状态）：

```
黑板内容：共 1 个对象
- [0] UserInput：本轮的提问
进程状态：
| 进程 ID  | quizzical_pascal |
| 状态     | RUNNING          |
| 运行时间 | 2.1 s            |
成本：本次 $0.0016 / 上限 $2.0000
Token：4,439 / 上限 1,000,000
模型：deepseek-flash（OpenAI）
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-tools-advanced spring-boot:run
```

## 代码结构

- `OrderTools.java` — `@UnfoldingTools` + 按 `category` 分组的 `@LlmTool`
- `LoggingInspector.java` — 同时实现 `ToolLoopInspector` 与 `ToolCallInspector`
- `ToolsAdvancedAgent.java` — 挂门面工具 + 回调 + 工具名纠正策略（目标类型 `Reply`）
- `IntrospectiveAgent.java` — 挂黑板/进程自省工具（目标类型 `Introspection`）
- `Introspection.java` — 自省报告；**刻意与 `Reply` 不同类型**，见下
- `ToolsAdvancedController.java` — `GET /tools-advanced/ask`、`GET /tools-advanced/inspect`

## 要点

- 渐进式工具的价值：工具很多时，初始提示词只放门面，**降低模型认知负担与 token 开销**。
- `category` 让模型**按需展开**（例如只展开 read，危险 write 工具默认不出现）。
- 回调是只读观测；要改写内容用 `ToolLoopTransformer`（例如长对话压缩）。
- 工具名纠正（`AutoCorrectionPolicy`）能显著降低"模型拼错工具名"导致的失败率。
- **一个模块里有多个 Agent 时，目标类型必须各不相同**：`AgentInvocation.create(platform, X.class)`
  是按 `findAgentByResultType` **按目标类型选 Agent** 的（没有按名字选的 API），
  共用同一目标类型会命中歧义。所以这里 `Reply` 与 `Introspection` 是两个类型。
- `GoalTool` / `AgentTool` 可把"某个目标 / 另一个 Agent"包装成工具（需注入 `Autonomy`），
  适合"让 Agent 自主调度其它目标/Agent"；进程内委派的更简单做法见 `embabel-subagent`。


## 附：工具循环的三个控制工具（未单独建模块）

本模块讲了"循环回调"（观测/改写）。框架还提供三个**控制**工具：

| 工具 | 作用 | 典型场景 |
|---|---|---|
| `OneShotPerLoopTool` | 包装后，该工具在**同一轮工具循环里只能被调用一次** | 防止模型反复调同一个"下单"工具造成重复副作用 |
| `LoopMemo` / `LoopMemoKt` | 在循环内**记忆/去重**（相同入参不重复执行） | 重复查询同一订单时直接返回缓存结果 |
| `ProgressTool` | 让工具上报**进度**（配合 `ProgressUpdateEvent`） | 长任务给用户可见进度 |

它们与"回调"的区别：回调是**只读观测**，这三个会**改变循环行为**。
如果发现模型在同一轮里重复调同一个工具，先考虑 `OneShotPerLoopTool` 或 `LoopMemo`，
而不是改提示词求它"别重复调"。
