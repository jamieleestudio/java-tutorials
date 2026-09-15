# embabel-tools-advanced — 工具进阶（渐进式工具 / 循环回调 / 工具名纠正）

## 演示内容

工具多了以后的三类工程问题：

1. **渐进式工具（progressive disclosure）**：把一组工具折叠成一个"门面工具"，模型按需展开
2. **工具循环回调**：观测每轮迭代与每次工具调用（也可用于改写对话历史）
3. **找不到工具的策略**：模型把工具名写错时自动纠正，而不是直接失败

## 关键 API

| API | 作用 |
|---|---|
| `@UnfoldingTools(name, description)` | 标在类上，把类里的 `@LlmTool` 折叠成门面工具 |
| `UnfoldingTool.fromInstance(obj)` | 生成门面 `Tool` |
| `@LlmTool(category = "read")` | 用类别分组，让模型按需选择展开哪一组 |
| `withToolLoopInspectors(...)` / `withToolCallInspectors(...)` | 循环级/单次调用级回调（只读观测） |
| `withToolLoopTransformers(...)` | 需要**改写**历史/结果时用（压缩、窗口化） |
| `withToolNotFoundPolicy(new AutoCorrectionPolicy(...))` | 工具名拼错时按相似度自动纠正 |

## 接口

```bash
curl -G --data-urlencode "message=帮我看看 A1001 这个订单的状态" http://localhost:8922/tools-advanced/ask
```

实测日志印证了门面机制：

```
(chat) starting tool loop [order_ops] max=20
(chat) calling tool order_ops({"category": "read"})
(chat) tool order_ops returned Tools now available: queryOrder, listOrders. You MUST call one of these ...
LoggingInspector - [tool-call] finished: AfterToolCallContext(...)
DefaultToolLoop - Strategy removed 1 tools after order_ops: [order_ops]     ← 展开后移除门面
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-tools-advanced spring-boot:run
```

## 代码结构

- `OrderTools.java` — `@UnfoldingTools` + 按 `category` 分组的 `@LlmTool`
- `LoggingInspector.java` — 同时实现 `ToolLoopInspector` 与 `ToolCallInspector`
- `ToolsAdvancedAgent.java` — 挂门面工具 + 回调 + 工具名纠正策略
- `ToolsAdvancedController.java` — `GET /tools-advanced/ask`

## 要点

- 渐进式工具的价值：工具很多时，初始提示词只放门面，**降低模型认知负担与 token 开销**。
- `category` 让模型**按需展开**（例如只展开 read，危险 write 工具默认不出现）。
- 回调是只读观测；要改写内容用 `ToolLoopTransformer`（例如长对话压缩）。
- 工具名纠正（`AutoCorrectionPolicy`）能显著降低"模型拼错工具名"导致的失败率。
