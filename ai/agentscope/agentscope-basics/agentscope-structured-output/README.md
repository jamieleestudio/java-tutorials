# agentscope-structured-output — 结构化输出

## 演示内容

用 `agent.call(List<Msg>, Class, RuntimeContext).block().getStructuredData(Class)` 让模型直接返回强类型对象。

```bash
curl -G --data-urlencode "product=DeepSeek V4 Flash 大模型" http://localhost:9102/structured/analyze
```

实测返回 `ProductAnalysis` record 的 JSON（含 productName / strengths / weaknesses / rating / conclusion）。

## 关键 API

| API | 作用 |
|---|---|
| `agent.call(List<Msg>, Class<?>, RuntimeContext)` | 传入目标类型，框架自动生成 JSON Schema 并解析 |
| `Msg.getStructuredData(Class)` | 从返回的 Msg 中提取结构化数据 |
| `@JsonPropertyDescription` | 给字段加描述（变成 schema 的 field description） |

## 与 Embabel 的对照

| | Embabel | AgentScope |
|---|---|---|
| 调用 | `ai.creating(X.class).fromPrompt(prompt)` | `agent.call(msgs, X.class, ctx).block().getStructuredData(X.class)` |
| 返回 | 直接返回 X | 返 `Msg`，再 `getStructuredData` 提取 |
| Schema | 框架从 Class 生成 | 框架从 Class 生成（`ToolSchemaGenerator`） |

## 代码结构

- `ProductAnalysis.java` — 目标类型（record）
- `StructuredOutputAgent.java` — `agent.call(msgs, Class, ctx)`
- `StructuredOutputController.java` — `GET /structured/analyze`