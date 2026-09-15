# embabel-tools — 工具调用（Function Calling）

## 演示内容

给 Agent 挂工具，由模型自主决定何时调用、必要时多轮调用。演示两种注册方式：

1. **注解对象**：`@LlmTool` 标注的方法 → `Tool.fromInstance(...)`
2. **函数式工具**：`Tool.create(name, description, handler)`

## 关键 API

| API | 作用 |
|---|---|
| `@LlmTool(description=...)` | 把方法暴露为工具；`@LlmTool.Param(description=...)` 描述参数 |
| `Tool.fromInstance(obj)` | 扫描对象上的 `@LlmTool` 方法，返回 `List<Tool>` |
| `Tool.create(name, desc, handler)` | 用函数式接口快速定义工具 |
| `promptRunner.withTools(List<Tool>)` | 把工具挂到本次 LLM 调用上 |

## 内置工具（示例）

`CalculatorTools` 提供 `add`、`multiply`、`weather`（模拟数据）、`now`；
`ToolChatAgent` 另外用 `Tool.create` 注册了一个 `echo` 工具。

## 接口

```bash
curl -G --data-urlencode "message=北京天气怎么样？另外帮我算一下 123 乘以 456" http://localhost:8891/tools/ask
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-tools spring-boot:run
```

## 代码结构

- `CalculatorTools.java` — `@LlmTool` 工具集
- `ToolChatAgent.java` — `@Action` 中 `Tool.fromInstance(...)` + `Tool.create(...)`
- `ToolsController.java` — `GET /tools/ask`

## 要点

- 工具的参数 schema 由 Embabel 自动生成（`TypeBasedInputSchema`），无需手写 JSON Schema。
- 模型是否调用工具、调用几次由 LLM 决定；失败/异常可通过 `Tool.Result.error(...)` 回传。
- 更多工具能力：`MathTools`、`FileReadTools`、`Subagent`、MCP 等（见其他模块）。
